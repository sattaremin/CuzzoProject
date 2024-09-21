package com.amazon.dto;

import com.amazon.custom_validation.UniqueClientVendorName;
import com.amazon.enums.ClientVendorType;
import lombok.*;

import javax.persistence.Column;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ClientVendorDto {

    private Long id;

    @Size(min = 2, max = 50, message = "Company Name must be between 2 and 50 characters long.")
    @UniqueClientVendorName
    @Column(unique = true)
    private String clientVendorName;

    @NotBlank(message = "Phone number is required field")
    @Pattern(regexp = "^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$" + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$" + "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$", message = "Phone Number is required field and may be in any valid phone number format.")

    @NotBlank(message = "Phone number is required field")
    @Pattern(regexp = "^\\+1 \\(\\d{3}\\) \\d{3}-\\d{4}$", message = "Phone Number is required field and may be in any valid phone number format.")
    private String phone;

    @Pattern(regexp = "^https:\\/\\/www\\.[a-zA-Z0-9-]+\\.com\\/?$", message = "Website should have a valid format.")
    private String website;

    @NotNull(message = "please select type.")
    private ClientVendorType clientVendorType;
    @Valid
    private AddressDto address;
    @Valid
    private CompanyDto company;
    private Boolean hasInvoice;
}
