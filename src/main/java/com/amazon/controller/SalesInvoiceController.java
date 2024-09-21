package com.amazon.controller;

import com.amazon.dto.InvoiceDto;
import com.amazon.dto.InvoiceProductDto;
import com.amazon.dto.ProductDto;
import com.amazon.enums.InvoiceType;
import com.amazon.exception.InsufficientStockException;
import com.amazon.exception.InvoiceNotFoundException;
import com.amazon.repository.InvoiceProductRepository;
import com.amazon.service.*;
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
public class SalesInvoiceController {

    private final InvoiceService invoiceService;
    private final ClientVendorService clientVendorService;
    private final SecurityService securityService;
    private final InvoiceProductService invoiceProductService;
    private final ProductService productService;

    private final InvoiceProductRepository invoiceProductRepository;

    private final InvoiceProductMapper invoiceProductMapper;

    public SalesInvoiceController(InvoiceService invoiceService, ClientVendorService clientVendorService,
                                  SecurityService securityService, InvoiceProductService invoiceProductService, ProductService productService, InvoiceProductRepository invoiceProductRepository, InvoiceProductMapper invoiceProductMapper) {
        this.invoiceService = invoiceService;
        this.clientVendorService = clientVendorService;
        this.securityService = securityService;
        this.invoiceProductService = invoiceProductService;
        this.productService = productService;
        this.invoiceProductRepository = invoiceProductRepository;
        this.invoiceProductMapper = invoiceProductMapper;
    }

    @GetMapping("/salesInvoices/list")
    public String listAllSalesInvoice(Model model) {
        model.addAttribute("invoices", invoiceService.listAllSalesInvoices());
        return "invoice/sales-invoice-list";
    }

    @GetMapping("/salesInvoices/create")
    public String createChaseInvoice(Model model) {
        model.addAttribute("newSalesInvoice", new InvoiceDto());
        model.addAttribute("clients", clientVendorService.listAllByCompanyTitle());
        model.addAttribute("invoiceNo", invoiceService.newInvoiceNo(InvoiceType.SALES));
        model.addAttribute("date", LocalDate.now());
        return "invoice/sales-invoice-create";
    }

    @PostMapping("/salesInvoices/create")
    public String saveChaseInvoice(@Valid @ModelAttribute("newSalesInvoice") InvoiceDto newSalesInvoice, BindingResult bindingResult,Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("clients", clientVendorService.listAllByCompanyTitle());
            model.addAttribute("invoiceNo", invoiceService.newInvoiceNo(InvoiceType.SALES));
            model.addAttribute("date", LocalDate.now());
            return "invoice/sales-invoice-create";
        }

        invoiceService.save(newSalesInvoice, InvoiceType.SALES);
        return "redirect:/salesInvoices/list";
    }

    @GetMapping("/salesInvoices/delete/{id}")
    public String deleteFromSalesInvoiceList(@PathVariable Long id, Model model) {

        invoiceService.removeInvoiceById(id);
        return "redirect:/salesInvoices/list";
    }

    @GetMapping("/salesInvoices/print/{id}")
    public String printSalesInvoice(@PathVariable Long id, Model model) {

        model.addAttribute("company", securityService.getCurrentUserCompany());

        model.addAttribute("invoice", invoiceProductService.calculateAndSetInvoiceTotals(id));

        List<InvoiceProductDto> invoiceProducts = invoiceProductService.listAllByInvoiceId(id).stream()
                .map(invoiceProductService::setInvoiceTotal)
                .collect(Collectors.toList());
        model.addAttribute("invoiceProducts", invoiceProducts);


        return "invoice/invoice_print";
    }

    @PostMapping("/salesInvoices/update/{invoiceId}")
    public String updateInvoicePost(@PathVariable("invoiceId") Long invoiceId, @Valid InvoiceDto invoice, BindingResult result, Model model) throws InvoiceNotFoundException {
        if (result.hasErrors()) {
            populateModel(model, invoiceId);
            return "invoice/sales-invoice-update";
        }
        invoiceService.save(invoice,InvoiceType.SALES);
        return "redirect:/salesInvoices/update/" + invoiceId;
    }
    @GetMapping("/salesInvoices/approve/{id}")
    public String getApproveInvoice(@PathVariable("id") Long id) throws InvoiceNotFoundException {
        InvoiceDto invoiceDto = invoiceService.findById(id);
        invoiceService.approveSalesInvoice(invoiceDto.getId());
        return "redirect:/salesInvoices/list";
    }

    @GetMapping("/salesInvoices/removeInvoiceProduct/{invoiceId}/{invoiceProductId}")
    public String removeInvoiceProduct(@PathVariable("invoiceId") Long invoiceId, @PathVariable("invoiceProductId") Long invoiceProductId) {
        invoiceService.removeInvoiceProduct(invoiceId, invoiceProductId);
        return "redirect:/salesInvoices/update/" + invoiceId;
    }

    @GetMapping("/salesInvoices/update/{id}")
    public String editSalesInvoice(@PathVariable("id") Long id, Model model) throws InvoiceNotFoundException {

        model.addAttribute("invoice", invoiceService.findById(id));
        model.addAttribute("newInvoiceProduct", new InvoiceProductDto());
        model.addAttribute("clients", clientVendorService.listClientVendorsByCompany());
        model.addAttribute("products", productService.listProductsInStock());
        model.addAttribute("invoiceProducts",invoiceProductService.listAllByInvoiceId(id));

        return "invoice/sales-invoice-update";
    }


    @PostMapping("/salesInvoices/addInvoiceProduct/{id}")
    public String addInvoiceProduct(@PathVariable Long id,
                                    @ModelAttribute("newInvoiceProduct") @Valid InvoiceProductDto newInvoiceProduct,
                                    BindingResult result, Model model) throws InvoiceNotFoundException {

        populateModel(model, id);

        if (result.hasErrors()) {
            return "invoice/sales-invoice-update";
        }

        Long productId = newInvoiceProduct.getProduct().getId();
        ProductDto product = productService.getProductById(productId);

        if (product == null) {
            result.rejectValue("product.id", "", "Product not found");
            return "invoice/sales-invoice-update";
        }

        try {
            BigDecimal totalCost = invoiceProductService.calculateTotal(
                    newInvoiceProduct.getPrice(),
                    newInvoiceProduct.getQuantity(),
                    BigDecimal.valueOf(newInvoiceProduct.getTax())
            );
            newInvoiceProduct.setTotal(totalCost);

            invoiceProductService.addInvoiceProduct(id, newInvoiceProduct);
        } catch (InsufficientStockException e) {
            result.rejectValue("quantity", "", "Not enough " + product.getName() + " quantity to sell...");
            return "invoice/sales-invoice-update";
        }

        return "redirect:/salesInvoices/update/" + id;
    }


    private void populateModel(Model model, Long invoiceId) throws InvoiceNotFoundException {
        model.addAttribute("invoice", invoiceService.findById(invoiceId));
        model.addAttribute("vendors", productService.findAllVendors());
        model.addAttribute("products", productService.listProductsInStock());
        model.addAttribute("invoiceProducts", invoiceProductService.getActiveProductsByInvoiceId(invoiceId));
    }







}
