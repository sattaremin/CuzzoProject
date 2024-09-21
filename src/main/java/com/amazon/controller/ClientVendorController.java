package com.amazon.controller;

import com.amazon.client.CountryClient;
import com.amazon.dto.ClientVendorDto;
import com.amazon.enums.ClientVendorType;
import com.amazon.record.CountryClientRequiredHeader;
import com.amazon.service.ClientVendorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("clientVendors")
public class ClientVendorController {

    private final ClientVendorService clientVendorService;
    private final CountryClient countryClient;
    private final List<ClientVendorType> CLIENT_VENDOR_TYPE = List.of(ClientVendorType.CLIENT, ClientVendorType.VENDOR);


    public ClientVendorController(ClientVendorService clientVendorService,CountryClient countryClient) {
        this.clientVendorService = clientVendorService;
        this.countryClient = countryClient;

    }

    @GetMapping("/list")
    public String listClientVendor(Model model) {

        model.addAttribute("clientVendors", clientVendorService.listAllClientVendors());
        return "/clientVendor/clientVendor-list";
    }

    @GetMapping("/create")
    public String createClientVendor(Model model) {

        model.addAttribute("newClientVendor", new ClientVendorDto());
        model.addAttribute("clientVendorTypes", CLIENT_VENDOR_TYPE);
        model.addAttribute("countries",
                countryClient.getCountries("Bearer " + countryClient.getAccessToken(
                                CountryClientRequiredHeader.ACCEPT_JSON,
                                CountryClientRequiredHeader.API_TOKEN,
                                CountryClientRequiredHeader.EMAIL).getAuthToken(),
                        CountryClientRequiredHeader.ACCEPT_JSON));
        return "clientVendor/clientVendor-create";
    }

    @PostMapping("/create")
    public String insertClientVendor(@Valid @ModelAttribute("newClientVendor") ClientVendorDto clientVendorDto,
                                     BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("clientVendorTypes", CLIENT_VENDOR_TYPE); // Ensure clientVendorTypes is available on errors
            model.addAttribute("countries",
                    countryClient.getCountries("Bearer " + countryClient.getAccessToken(
                                    CountryClientRequiredHeader.ACCEPT_JSON,
                                    CountryClientRequiredHeader.API_TOKEN,
                                    CountryClientRequiredHeader.EMAIL).getAuthToken(),
                            CountryClientRequiredHeader.ACCEPT_JSON));
            return "clientVendor/clientVendor-create";
        }
        clientVendorService.save(clientVendorDto);
        return "redirect:/clientVendors/list";
    }


    @GetMapping("/update/{id}")
    public String editClientVendor(@PathVariable("id") Long clientVendorID, Model model) {

        model.addAttribute("clientVendorTypes", CLIENT_VENDOR_TYPE);
        model.addAttribute("clientVendor", clientVendorService.findById(clientVendorID));
        model.addAttribute("clientVendorType", clientVendorService.findById(clientVendorID).getClientVendorType());
        return "/clientVendor/clientVendor-update";
    }

    @PostMapping("/update/{id}")
    public String updateClientVendor(@Valid @ModelAttribute("clientVendor") ClientVendorDto clientVendorDto,
                                     BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("clientVendorTypes", CLIENT_VENDOR_TYPE);
            return "clientVendor/clientVendor-update";

        }
        clientVendorService.save(clientVendorDto);
        return "redirect:/clientVendors/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteClientVendor(@PathVariable("id") Long clientVendorID, RedirectAttributes redirectAttributes) {


        try {
            clientVendorService.delete(clientVendorID);
            redirectAttributes.addFlashAttribute("message", "Client/Vendor successfully deleted.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }


        return "redirect:/clientVendors/list";
    }
}

