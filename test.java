import java.util.concurrent.atomic.AtomicBoolean;

public class YourClass {
    private static final Logger log = LoggerFactory.getLogger(YourClass.class);
    private static final AtomicBoolean hasLogged = new AtomicBoolean(false);

    @Transient
    public String getDataForAuditLog() {
        StringBuilder sbAuditData = new StringBuilder();
        try {
            Audit audit = getModel(details);
            String userIdHash = CryptoHelper.getHashDigest(audit.getUserId());
            String groupIdHash = CryptoHelper.getHashDigest(audit.getGroupId());

            sbAuditData.append(Constants.RECORD_DETAIL_IDENTIFIER)
                    .append(Constants.DELIMITER)
                    .append(StringUtils.isNotBlank(userIdHash) ? userIdHash : "")
                    .append(Constants.DELIMITER)
                    .append(StringUtils.isNotBlank(groupIdHash) ? groupIdHash : "");

        } catch (SQLException | IOException e) {
            if (hasLogged.compareAndSet(false, true)) {
                log.error("Exception occurred in getDataForAuditLog: {}", e.getMessage(), e);
            }
        }
        return sbAuditData.toString();
    }
}