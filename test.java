import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Make the all-args constructor private
@Getter
@Entity
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
    private byte[] publicKeyData; // Mutable field

    @Column(name = "PRIVATE_KEY")
    private String privateKey;

    @Column(name = "EXP_DATE")
    private Date expiryDate; // Mutable field

    @Column(name = "T_CREATED")
    private Timestamp createdOn; // Mutable field

    // Use Builder to control the setting of mutable fields
    @Builder
    public static BankKey create(Long id, String user, String groupId, String keyId, byte[] publicKeyData, String privateKey, Date expiryDate, Timestamp createdOn) {
        BankKey bankKey = new BankKey();
        bankKey.id = id;
        bankKey.user = user;
        bankKey.groupId = groupId;
        bankKey.keyId = keyId;
        bankKey.setPublicKeyData(publicKeyData); // Defensive copy
        bankKey.privateKey = privateKey;
        bankKey.setExpiryDate(expiryDate); // Defensive copy
        bankKey.setCreatedOn(createdOn); // Defensive copy
        return bankKey;
    }

    // Defensive copy for mutable fields
    public void setPublicKeyData(byte[] publicKeyData) {
        this.publicKeyData = publicKeyData == null ? null : publicKeyData.clone();
    }

    public byte[] getPublicKeyData() {
        return this.publicKeyData == null ? null : this.publicKeyData.clone();
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate == null ? null : new Date(expiryDate.getTime());
    }

    public Date getExpiryDate() {
        return this.expiryDate == null ? null : new Date(this.expiryDate.getTime());
    }

    public void setCreatedOn(Timestamp createdOn) {
        this.createdOn = createdOn == null ? null : new Timestamp(createdOn.getTime());
    }

    public Timestamp getCreatedOn() {
        return this.createdOn == null ? null : new Timestamp(this.createdOn.getTime());
    }
}
