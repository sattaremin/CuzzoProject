package com.amazon.controller;

import com.amazon.client.CountryClient;
import com.amazon.dto.CompanyDto;
import com.amazon.enums.CompanyStatus;
import com.amazon.record.CountryClientRequiredHeader;
import com.amazon.repository.CompanyRepository;
import com.amazon.service.CompanyService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequestMapping("/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final CountryClient countryClient;
    private final CompanyRepository companyRepository;

    public CompanyController(CompanyService companyService, CountryClient countryClient, CompanyRepository companyRepository) {
        this.companyService = companyService;
        this.countryClient = countryClient;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/list")
    public String listAllCompanies(Model model) {

        model.addAttribute("companies", companyService.findAllExcludingCompanyWithIdAndSorted(1L));

        return "/company/company-list";

    }

    @GetMapping("/create")
    public String createCompany(Model model) {
        model.addAttribute("newCompany", new CompanyDto());
        model.addAttribute("countries",
                countryClient.getCountries("Bearer " + countryClient.getAccessToken(
                                CountryClientRequiredHeader.ACCEPT_JSON,
                                CountryClientRequiredHeader.API_TOKEN,
                                CountryClientRequiredHeader.EMAIL).getAuthToken(),
                        CountryClientRequiredHeader.ACCEPT_JSON));

        return "/company/company-create";
    }

    @PostMapping("/create")
    public String insertCompany(@Valid @ModelAttribute("newCompany") CompanyDto company,
                                BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("countries",
                    countryClient.getCountries("Bearer " + countryClient.getAccessToken(
                                    CountryClientRequiredHeader.ACCEPT_JSON,
                                    CountryClientRequiredHeader.API_TOKEN,
                                    CountryClientRequiredHeader.EMAIL).getAuthToken(),
                            CountryClientRequiredHeader.ACCEPT_JSON));
            return "/company/company-create";
        }
        companyService.create(company);
        redirectAttributes.addFlashAttribute("successMessage", "Company created successfully");
        return "redirect:/companies/list";
    }

    @GetMapping("/update/{id}")
    public String editCompany(@PathVariable("id") Long id, Model model) {
        model.addAttribute("company", companyService.findById(id));
        return "/company/company-update";
    }

    @PostMapping("/update/{id}")
    public String updateCompany(@Valid @ModelAttribute("company") CompanyDto companyDto,
                                BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "/company/company-update";
        }
        companyService.update(companyDto);
        redirectAttributes.addFlashAttribute("successMessage", "Company updated successfully");
        return "redirect:/companies/list";
    }

    @GetMapping("/activate/{id}")
    public String activateCompany(@PathVariable Long id) {
        companyService.updateStatus(id, CompanyStatus.ACTIVE);
        return "redirect:/companies/list";
    }

    @GetMapping("/deactivate/{id}")
    public String deactivateCompany(@PathVariable Long id) {
        companyService.updateStatus(id, CompanyStatus.PASSIVE);
        return "redirect:/companies/list";
    }
}