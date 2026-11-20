package com.firststep.tcg.product;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={
    "spring.datasource.url=jdbc:h2:mem:product-tests;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@org.springframework.test.context.ActiveProfiles("test")
@AutoConfigureMockMvc
class ProductImagesIntegrationTest {
    @TempDir static Path directory;
    @DynamicPropertySource static void storage(DynamicPropertyRegistry properties) {
        properties.add("app.product-images.directory", () -> directory.toString());
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProductRepository products;
    @Autowired org.springframework.jdbc.core.JdbcTemplate jdbc;

    MockMultipartFile product(String name) {
        return new MockMultipartFile("product", "product.json", "application/json",
            ("{\"name\":\"" + name + "\",\"description\":\"Test\",\"price\":\"350.00\",\"stock\":10}").getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    MockMultipartFile png(String name) throws Exception {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB), "png", output);
        return new MockMultipartFile("images", name, "image/png", output.toByteArray());
    }

    @Test void savesOrderedImagesAndServesThemWithoutLogin() throws Exception {
        var result = mvc.perform(multipart("/api/admin/products").file(product("Gallery"))
            .file(png("../../cover.png")).file(png("second.png"))
            .with(user("admin").roles("ADMIN")).with(csrf()))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.imageUrls.length()").value(2)).andReturn();
        var body = json.readTree(result.getResponse().getContentAsString());
        Long id = body.get("id").asLong();
        String first = body.get("imageUrls").get(0).asText();
        String second = body.get("imageUrls").get(1).asText();
        assertThat(first).matches("/api/product-images/[a-f0-9-]{36}\\.png");
        assertThat(products.findById(id).orElseThrow().getImageUrls()).containsExactly(first, second);
        mvc.perform(get(first)).andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_PNG));
        mvc.perform(get("/api/admin/products").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk());
    }

    @Test void allowsNoImagesAndKeepsLegacyJsonApi() throws Exception {
        mvc.perform(multipart("/api/admin/products").file(product("No images"))
            .with(user("admin").roles("ADMIN")).with(csrf()))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.imageUrls").isEmpty());
        mvc.perform(post("/api/admin/products").with(user("admin").roles("ADMIN")).with(csrf())
            .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Legacy\",\"price\":0,\"stock\":0}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.imageUrls").isEmpty());
    }

    @Test void rejectsFakeImagesAndCleansEarlierFiles() throws Exception {
        long before = countFiles();
        var fake = new MockMultipartFile("images", "fake.png", "image/png", "not a picture".getBytes());
        mvc.perform(multipart("/api/admin/products").file(product("Fake"))
            .file(png("valid.png")).file(fake).with(user("admin").roles("ADMIN")).with(csrf()))
            .andExpect(status().isBadRequest());
        assertThat(countFiles()).isEqualTo(before);
    }

    @Test void rejectsTooManyAndOversizedImages() throws Exception {
        var request = multipart("/api/admin/products").file(product("Too many"));
        for (int i = 0; i < 6; i++) request.file(png(i + ".png"));
        mvc.perform(request.with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/admin/products").file(product("Too large"))
            .file(new MockMultipartFile("images", "large.png", "image/png", new byte[5 * 1024 * 1024 + 1]))
            .with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().isBadRequest());
    }

    @Test void cleansFilesWhenProductValidationFails() throws Exception {
        long before = countFiles();
        long productsBefore = products.count();
        mvc.perform(multipart("/api/admin/products").file(product(" ")).file(png("valid.png"))
            .with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().isBadRequest());
        assertThat(countFiles()).isEqualTo(before);
        assertThat(products.count()).isEqualTo(productsBefore);
    }

    @Test void uploadRequiresAdminAndCsrf() throws Exception {
        mvc.perform(multipart("/api/admin/products").file(product("Guest")).with(csrf()))
            .andExpect(status().isUnauthorized());
        mvc.perform(multipart("/api/admin/products").file(product("Customer"))
            .with(user("customer").roles("CUSTOMER")).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(multipart("/api/admin/products").file(product("No CSRF"))
            .with(user("admin").roles("ADMIN"))).andExpect(status().isForbidden());
    }

    @Test void missingImageReturnsNotFound() throws Exception {
        mvc.perform(get("/api/product-images/not-a-file.png")).andExpect(status().isNotFound());
    }

    @Test void publicCatalogExcludesHiddenProducts() throws Exception {
        mvc.perform(multipart("/api/admin/products").file(product("Public gallery product"))
            .with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().isCreated()).andReturn();
        var hidden = mvc.perform(multipart("/api/admin/products").file(product("Hidden gallery product"))
            .with(user("admin").roles("ADMIN")).with(csrf())).andExpect(status().isCreated()).andReturn();
        long hiddenId = json.readTree(hidden.getResponse().getContentAsString()).get("id").asLong();
        jdbc.update("update products set active = false where id = ?", hiddenId);
        var catalog = mvc.perform(get("/api/products")).andExpect(status().isOk()).andReturn();
        var names = new java.util.ArrayList<String>();
        json.readTree(catalog.getResponse().getContentAsString()).forEach(item -> names.add(item.get("name").asText()));
        assertThat(names).contains("Public gallery product").doesNotContain("Hidden gallery product");
    }

    long countFiles() throws Exception {
        try (var files = Files.list(directory)) { return files.count(); }
    }
}
