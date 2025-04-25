package wsulu.document.service;

public interface DocumentZipService {

    /**
     * Receiving a zip archive with all client documents
     * @param loanId loan identifier in the database
     * @param clientId client identifier in the database
     * @param allVersion if true - all versions of the client's documents are loaded, otherwise only the latest version is loaded
     * @return zip archive with client documents as a byte array
     */
    byte[] getAllDocumentByLoanId(Long loanId, Long clientId, boolean allVersion);
}
