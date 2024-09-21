package com.amazon.repository;

import com.amazon.entity.Company;
import com.amazon.entity.Invoice;
import com.amazon.enums.InvoiceStatus;
import com.amazon.enums.InvoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDesc(InvoiceType invoiceType, String companyTitle);

    @Query("SELECT i FROM Invoice i WHERE i.invoiceType = :invoiceType AND i.company.title = :companyTitle AND i.isDeleted = false ORDER BY i.invoiceNo DESC")
    List<Invoice> findAllByInvoiceTypeAndCompany_TitleOrderByInvoiceNoDescIsDeletedTrue(@Param("invoiceType") InvoiceType invoiceType, @Param("companyTitle") String companyTitle);

    //List<Invoice> findAllByIsDeletedAndCompany_TitleOrderByInvoiceNoDescAndInvoiceType(Boolean isDeleted, String companyTitle, InvoiceType invoiceType);

    List<Invoice> findAllByInvoiceType(InvoiceType invoiceType);

    List<Invoice> findAllByInvoiceTypeAndInvoiceStatusAndCompanyTitle(InvoiceType invoiceType, InvoiceStatus invoiceStatus, String companyTitle);

    List<Invoice> findTop3ByInvoiceStatusOrderByDateDesc(InvoiceStatus invoiceStatus);

    List<Invoice> findAllByInvoiceStatusAndCompany(InvoiceStatus status, Company company);
    List<Invoice> findByClientVendorId(Long clientVendorId);
    List<Invoice> findAllByInvoiceTypeAndCompany_Id(InvoiceType invoiceType, Long companyId);

    @Query("SELECT i FROM Invoice i WHERE i.id = :invoiceId AND i.company.id = :companyId AND i.invoiceStatus = :status")
    Invoice findByIdAndCompanyIdAndInvoiceStatus(
            @Param("invoiceId") Long invoiceId,
            @Param("companyId") Long companyId,
            @Param("status") InvoiceStatus status
    );



}
