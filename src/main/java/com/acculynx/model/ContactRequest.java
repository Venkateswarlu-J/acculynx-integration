package com.acculynx.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ContactRequest {

    @JsonProperty("firstName")
    private final String firstName;

    @JsonProperty("lastName")
    private final String lastName;

    @JsonProperty("phoneNumbers")
    private final List<PhoneNumber> phoneNumbers;

    @JsonProperty("emailAddresses")
    private final List<EmailAddress> emailAddresses;

    @JsonProperty("contactTypeIds")
    private final List<String> contactTypeIds =
            List.of("52ba94c5-3ecf-4e7f-90cd-a91de12a72f5");

    public ContactRequest(String firstName, String lastName,
                          String phone, String email) {
        this.firstName      = firstName;
        this.lastName       = lastName;
        this.phoneNumbers   = List.of(new PhoneNumber(phone));
        this.emailAddresses = List.of(new EmailAddress(email));
    }

    public String getFirstName()                    { return firstName;      }
    public String getLastName()                     { return lastName;       }
    public List<PhoneNumber> getPhoneNumbers()      { return phoneNumbers;   }
    public List<EmailAddress> getEmailAddresses()   { return emailAddresses; }
    public List<String> getContactTypeIds()         { return contactTypeIds; }

    // ── Nested: PhoneNumber ──
    public static class PhoneNumber {
        @JsonProperty("number")
        private final String number;

        @JsonProperty("isPrimary")
        private final boolean isPrimary = true;

        public PhoneNumber(String number) { this.number = number; }
        public String getNumber()         { return number;        }
        public boolean isPrimary()        { return isPrimary;     }
    }

    // ── Nested: EmailAddress ──
    public static class EmailAddress {
        @JsonProperty("address")
        private final String address;

        @JsonProperty("isPrimary")
        private final boolean isPrimary = true;

        public EmailAddress(String address) { this.address = address; }
        public String getAddress()          { return address;         }
        public boolean isPrimary()          { return isPrimary;       }
    }
}