package wsulu.document.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wsulu.document.model.DocumentsContractEntity;

import java.util.List;

@Repository
public interface DocumentsContractRepo extends JpaRepository<DocumentsContractEntity, Long> {

    @Query(
            value =
                    "SELECT * FROM documents_contract as dc "
                            + "WHERE dc.loan_id=:loanId and dc.client_id=:clientId "
                            + "order by id desc limit 1",
            nativeQuery = true)
    DocumentsContractEntity findFirstByLoanId(
            @Param("loanId") Long loanId, @Param("clientId") Long clientId);

    @Query(value = "SELECT count(doc.id) FROM documents_contract doc WHERE doc.mobile=:mobile")
    long countByMobile(@Param("mobile") String mobile);

    @Query(
            value =
                    "SELECT * FROM documents_contract doc "
                            + "WHERE doc.client_id=:clientId order by doc.id desc limit 1",
            nativeQuery = true)
    DocumentsContractEntity findFirstByClientIdOrderByIdDesc(@Param("clientId") Long clientId);

    @Query(
            value =
                    "SELECT * FROM documents_contract doc "
                            + "WHERE doc.contract_number=:contractNumber order by doc.id desc limit 1",
            nativeQuery = true)
    DocumentsContractEntity findFirstByContractNumberOrderByIdDesc(
            @Param("contractNumber") String contractNumber);

    @Query(value = "SELECT doc FROM documents_contract doc WHERE doc.mobile=:mobile")
    List<DocumentsContractEntity> findAllByMobile(@Param("mobile") String mobile);
}
