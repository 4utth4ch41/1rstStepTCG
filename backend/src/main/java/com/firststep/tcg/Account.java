package com.firststep.tcg;
import jakarta.persistence.*;
@Entity
@Table(name="accounts", uniqueConstraints=@UniqueConstraint(columnNames="email"))
public class Account {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @Column(nullable=false, length=254) String email;
 @Column(nullable=false, length=80) String displayName;
 @Column(nullable=false) String passwordHash;
 @Column(nullable=false) String role;
 protected Account() {}
 Account(String email, String displayName, String passwordHash, String role) {
  this.email=email; this.displayName=displayName; this.passwordHash=passwordHash; this.role=role;
 }
}
