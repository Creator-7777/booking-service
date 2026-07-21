package com.alena.booking.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NailService {

    MANICURE(120),

    MANICURE_STRENGTH(200),

    EXTENSION(350),

    NAIL_REPAIR(10),

    NAIL_EXTENSION_REPAIR(20),

    FRENCH(30),

    SIMPLE_DESIGN(10),

    COMPLEX_DESIGN(30),

    KING_NAIL(50),

    CLEANING(40),

    PEDICURE_TOES(160),

    PEDICURE_GEL(220),

    PEDICURE_CLASSIC(200),

    CALLUS_REMOVAL(50),

    CRACK_TREATMENT(50),

    INGROWN_NAIL(50),

    TAMPONADE(50);

    private final Integer price;
}