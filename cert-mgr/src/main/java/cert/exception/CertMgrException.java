package cert.exception;


import cert.enums.MgrExceptionCodeEnums;

/**
 * @author wesleywang
 * @Description:
 * @date 2020/8/28
 */
public class CertMgrException extends Exception {
    /** @Fields serialVersionUID : TODO */
    private static final long serialVersionUID = 893822168485972751L;
    private MgrExceptionCodeEnums ece;

    public CertMgrException(MgrExceptionCodeEnums ece) {
        super(ece.getExceptionMessage());
        this.ece = ece;
    }

    public CertMgrException(String msg) {
        super(msg);
        this.ece.setExceptionMessage(msg);
    }

    public MgrExceptionCodeEnums getCodeMessageEnums() {
        return ece;
    }
}