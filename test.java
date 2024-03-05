import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "PGP_BANK_KEYS")
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
    private byte[] publicKeyData;

    @Column(name = "PRIVATE_KEY")
    private String privateKey;

    @Column(name = "EXP_DATE")
    private Date expiryDate;

    @Column(name = "T_CREATED")
    private Timestamp createdOn;

    // Custom setter for mutable and sensitive fields
    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = (expiryDate == null) ? null : new Date(expiryDate.getTime());
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = (createdOn == null) ? null : new Timestamp(createdOn.getTime());
    }

    // Exclude from Lombok and manually implement to include encryption or security measures
    public void setPrivateKey(String privateKey) {
        // Placeholder for actual encryption logic
        this.privateKey = encryptPrivateKey(privateKey);
    }

    // Builder customization to include safe handling for mutable and sensitive fields
    public static class BankKeyBuilder {
        private Date expiryDate;
        private Timestamp createdOn;
        private String privateKey;

        public BankKeyBuilder expiryDate(Date expiryDate) {
            this.expiryDate = (expiryDate == null) ? null : new Date(expiryDate.getTime());
            return this;
        }

        public BankKeyBuilder createdOn(Timestamp createdOn) {
            this.createdOn = (createdOn == null) ? null : new Timestamp(createdOn.getTime());
            return this;
        }

        public BankKeyBuilder privateKey(String privateKey) {
            // Placeholder for actual encryption logic
            this.privateKey = encryptPrivateKey(privateKey);
            return this;
        }

        // Placeholder for the encryption method
        private static String encryptPrivateKey(String privateKey) {
            // Implement encryption logic here
            return privateKey; // Modify this with actual encryption
        }
    }

    // Additional logic...
}
