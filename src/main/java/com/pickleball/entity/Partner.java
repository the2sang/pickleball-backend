package com.pickleball.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "partner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Partner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", unique = true)
    private Long accountId;

    @Column(name = "business_partner", nullable = false, length = 100)
    private String businessPartner;

    @Column(nullable = false, length = 100)
    private String owner;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "partner_address", nullable = false, length = 200)
    private String partnerAddress;

    /**
     * 사업주 등급: 0 = 승인요청(디폴트), 1 = 승인완료
     */
    @Builder.Default
    @Column(name = "partner_level", nullable = false, length = 10)
    private String partnerLevel = "0";

    @Column(name = "partner_email", nullable = false, length = 100)
    private String partnerEmail;

    @Column(name = "regist_date", nullable = false)
    private LocalDate registDate;

    @Column(name = "partner_account", length = 50)
    private String partnerAccount;

    @Column(name = "partner_bank", length = 100)
    private String partnerBank;

    @Column(name = "how_to_pay", length = 10)
    private String howToPay;

    @Column(name = "create_date", nullable = false, updatable = false)
    private LocalDateTime createDate;

    @Column(name = "update_date")
    private LocalDateTime updateDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateDate = LocalDateTime.now();
    }
}
