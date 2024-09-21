package com.amazon.controller;


import com.amazon.dto.InvoiceDto;
import com.amazon.dto.InvoiceProductDto;
import com.amazon.entity.InvoiceProduct;
import com.amazon.enums.InvoiceType;
import com.amazon.exception.InvoiceNotFoundException;
import com.amazon.repository.InvoiceProductRepository;
import com.amazon.service.*;
import com.amazon.util.InvoiceMapper;
import com.amazon.util.InvoiceProductMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller

public class PurchaseInvoiceController {


    private final InvoiceService invoiceService;
    private final ClientVendorService clientVendorService;
    private final InvoiceProductService invoiceProductService;
    private final SecurityService securityService;
    private final InvoiceProductMapper invoiceProductMapper;
    private final InvoiceProductRepository invoiceProductRepository;
    private final ProductService productService;

    private final InvoiceMapper invoiceMapper;


    public PurchaseInvoiceController(InvoiceService invoiceService, ClientVendorService clientVendorService, InvoiceProductService invoiceProductService, SecurityService securityService, InvoiceProductMapper invoiceProductMapper, InvoiceProductRepository invoiceProductRepository, ProductService productService, InvoiceMapper invoiceMapper) {
        this.invoiceService = invoiceService;
        this.clientVendorService = clientVendorService;
        this.invoiceProductService = invoiceProductService;
        this.securityService = securityService;
        this.productService = productService;
        this.invoiceProductRepository = invoiceProductRepository;
        this.invoiceProductMapper = invoiceProductMapper;

        this.invoiceMapper = invoiceMapper;
    }


    @GetMapping("/purchaseInvoices/list")
    public String listPurchaseInvoices(Model model) {
        List<InvoiceDto> purchaseInvoices = invoiceService.listAllPurchaseInvoices();
        model.addAttribute("invoices", purchaseInvoices);
        return "/invoice/purchase-invoice-list";
    }


    @GetMapping("/purchaseInvoices/create")
    public String createPurchaseInvoice(Model model) {
        model.addAttribute("newPurchaseInvoice", new InvoiceDto());
        model.addAttribute("vendors", clientVendorService.listAllByCompanyTitle());
        model.addAttribute("invoiceNo", invoiceService.newInvoiceNo(InvoiceType.PURCHASE));
        model.addAttribute("date", LocalDate.now());
        return "invoice/purchase-invoice-create";
    }

    @PostMapping("/purchaseInvoices/create")
    public String savePurchaseInvoice(@Valid @ModelAttribute("newPurchaseInvoice") InvoiceDto newPurchaseInvoice, BindingResult bindingResult,Model model) {
       if (bindingResult.hasErrors()) {
                   model.addAttribute("vendors", clientVendorService.listAllByCompanyTitle());
                   model.addAttribute("invoiceNo", invoiceService.newInvoiceNo(InvoiceType.PURCHASE));
                   model.addAttribute("date", LocalDate.now());
                   return "invoice/purchase-invoice-create";
               }

               invoiceService.save(newPurchaseInvoice, InvoiceType.PURCHASE);
               return "redirect:/purchaseInvoices/list";
    }


    @GetMapping("/purchaseInvoices/approve/{id}")
    public String getApproveInvoice(@PathVariable("id") Long id) throws InvoiceNotFoundException {

        invoiceService.approvePurchaseInvoice(id);
        return "redirect:/purchaseInvoices/list";
    }

    @GetMapping("/purchaseInvoices/print/{id}")
    public String printInvoice(@PathVariable Long id, Model model) {
        model.addAttribute("company", securityService.getCurrentUserCompany());
        InvoiceDto invoice = invoiceProductService.calculateAndSetInvoiceTotals(id);

        model.addAttribute("invoice", invoice);

        System.out.println("Invoice Number: " + invoice.getInvoiceNo());

        List<InvoiceProductDto> invoiceProducts = invoiceProductService.listAllByInvoiceId(id).stream()
                .map(invoiceProductService::setInvoiceTotal)
                .collect(Collectors.toList());
        model.addAttribute("invoiceProducts", invoiceProducts);


        return "invoice/invoice_print";
    }

    @GetMapping("/purchaseInvoices/delete/{id}")
    public String deleteInvoice(@PathVariable Long id, Model model) throws InvoiceNotFoundException {
        InvoiceDto invoiceDto = invoiceService.findById(id);

        invoiceService.removeInvoiceById(id);
        model.addAttribute("invoice", invoiceDto);
        return "redirect:/purchaseInvoices/list";
    }

    @GetMapping("/purchaseInvoices/update/{id}")
    public String editPurchaseInvoice(@PathVariable Long id, Model model) throws InvoiceNotFoundException {
        InvoiceDto invoice = invoiceService.findById(id);
        model.addAttribute("invoice", invoice);
        model.addAttribute("vendors", productService.findAllVendors());
        model.addAttribute("products", productService.findAllInStockProducts());
        model.addAttribute("invoiceProducts", invoiceService.findAllInvoiceProductsByInvoiceId(id));
        model.addAttribute("newInvoiceProduct", new InvoiceProductDto());
        return "/invoice/purchase-invoice-update";
    }

    @PostMapping("/purchaseInvoices/update/{id}")
    public String updatePurchaseInvoice(@PathVariable Long id, @ModelAttribute("invoice") @Valid InvoiceDto invoiceDto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("vendors", productService.findAllVendors());
            model.addAttribute("products", productService.findAllInStockProducts());
            model.addAttribute("invoiceProducts", invoiceService.findAllInvoiceProductsByInvoiceId(id));
            model.addAttribute("newInvoiceProduct", new InvoiceProductDto());
            return "/invoice/purchase-invoice-update";
        }
        invoiceService.updateInvoice(invoiceDto);
        return "redirect:/purchaseInvoices/list";
    }

    @PostMapping("/purchaseInvoices/addInvoiceProduct/{id}")
    public String addInvoiceProduct(@PathVariable Long id, @ModelAttribute("newInvoiceProduct") @Valid InvoiceProductDto newInvoiceProduct, BindingResult result, Model model) throws InvoiceNotFoundException {
        if (result.hasErrors()) {
            model.addAttribute("invoice", invoiceService.findById(id));
            model.addAttribute("vendors", productService.findAllVendors());
            model.addAttribute("products", productService.findAllInStockProducts());
            model.addAttribute("invoiceProducts", invoiceService.findAllInvoiceProductsByInvoiceId(id));
            return "/invoice/purchase-invoice-update";
        }BigDecimal totalCost = invoiceProductService.calculateTotal(
                newInvoiceProduct.getPrice(),
                newInvoiceProduct.getQuantity(),
                BigDecimal.valueOf(newInvoiceProduct.getTax())
        );
        newInvoiceProduct.setTotal(totalCost);
        try {
            invoiceService.addInvoiceProduct(id, newInvoiceProduct);
      

            newInvoiceProduct.setTotal(totalCost);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding product to invoice: " + e.getMessage());
            model.addAttribute("invoice", invoiceService.findById(id));
            model.addAttribute("vendors", productService.findAllVendors());
            model.addAttribute("products", productService.findAllInStockProducts());
            model.addAttribute("invoiceProducts", invoiceService.findAllInvoiceProductsByInvoiceId(id));
            return "/invoice/purchase-invoice-update";
        }


        List<InvoiceProduct> invoiceProducts = invoiceProductRepository.findAllByInvoiceId(id);
        invoiceProducts.add(invoiceProductMapper.convertToEntity(newInvoiceProduct));


        return "redirect:/purchaseInvoices/update/" + id;
    }

    @GetMapping("/purchaseInvoices/removeInvoiceProduct/{invoiceId}/{invoiceProductId}")
    public String removeInvoiceProduct(@PathVariable Long invoiceId, @PathVariable Long invoiceProductId) {
        invoiceService.removeInvoiceProduct(invoiceId, invoiceProductId);
        return "redirect:/purchaseInvoices/update/" + invoiceId;
    }


}



