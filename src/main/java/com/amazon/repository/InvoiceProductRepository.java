package com.amazon.repository;

import com.amazon.entity.InvoiceProduct;
import com.amazon.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface InvoiceProductRepository extends JpaRepository<InvoiceProduct, Long> {

    List<InvoiceProduct> findAllInvoiceProductsByInvoiceId(Long invoiceId);

    @Query("select i from InvoiceProduct i where i.invoice.id=?1 and i.isDeleted=false")
    List<InvoiceProduct> retrieveAllByInvoice_IdAndIsDeletedFalse(Long invoiceId);

    List<InvoiceProduct> findAllByInvoiceId(Long id);
    List<InvoiceProduct> findAllByInvoiceIdAndIsDeleted(Long id, boolean isDeleted);

    @Query("SELECT ip FROM InvoiceProduct ip WHERE ip.invoice.id = :invoiceId AND ip.isDeleted = false")
    List<InvoiceProduct> findActiveProductsByInvoiceId(Long invoiceId);

    @Query("SELECT ip FROM InvoiceProduct ip WHERE ip.invoice.invoiceStatus = :invoiceStatus ORDER BY ip.invoice.date DESC")
    List<InvoiceProduct> findApprovedInvoices(InvoiceStatus invoiceStatus);

    @Query("SELECT ip FROM InvoiceProduct ip WHERE ip.invoice.id = :invoiceId AND ip.product.id = :productId AND ip.isDeleted = false")
    List<InvoiceProduct> findAllByInvoiceIdAndProductId(Long invoiceId, Long productId);

    @Query("SELECT COUNT(ip) > 0 FROM InvoiceProduct ip WHERE ip.product.id = :productId")
    boolean existsInvoiceWithProduct(@Param("productId") Long productId);

}
