import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "PGP_BANK_KEYS")
@NoArgsConstructor
@Getter // Generates getters for all fields but with custom implementations for mutable types
public class BankKey implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USER_ID")
    private String user;

    @Column(name = "GROUP_ID")
    private String groupId;

    @Column(name = "KEY_ID")
    private String keyId;

    @Lob
    @Column(name = "PUBLIC_KEY_DATA")
    private byte[] publicKeyData; // Mutable field

    @Column(name = "PRIVATE_KEY")
    private String privateKey;

    @Column(name = "EXP_DATE")
    private Date expiryDate; // Mutable field

    @Column(name = "T_CREATED")
    private Timestamp createdOn; // Mutable field

    // Custom builder method to handle mutable fields properly
    public static class BankKeyBuilder {
        private byte[] publicKeyData;
        private Date expiryDate;
        private Timestamp createdOn;

        public BankKeyBuilder publicKeyData(byte[] publicKeyData) {
            this.publicKeyData = publicKeyData == null ? null : publicKeyData.clone();
            return this;
        }

        public BankKeyBuilder expiryDate(Date expiryDate) {
            this.expiryDate = expiryDate == null ? null : new Date(expiryDate.getTime());
            return this;
        }

        public BankKeyBuilder createdOn(Timestamp createdOn) {
            this.createdOn = createdOn == null ? null : new Timestamp(createdOn.getTime());
            return this;
        }

        // Build method will be automatically implemented by Lombok to use these custom setters
    }

    // Use Lombok to generate builder
    @Builder(builderMethodName = "newBuilder")

    // Defensive getters
    public byte[] getPublicKeyData() {
        return this.publicKeyData == null ? null : this.publicKeyData.clone();
    }

    public Date getExpiryDate() {
        return this.expiryDate == null ? null : new Date(this.expiryDate.getTime());
    }

    public Timestamp getCreatedOn() {
        return this.createdOn == null ? null : new Timestamp(this.createdOn.getTime());
    }

    // Note: Setters for mutable fields are intentionally omitted to enforce immutability
}
