package ru.proitr.tru.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TruFileType {
    TRU_MSP (1, "ТРУ для осуществления закупок среди МСП", "ListsGWS"),
    TRU_IP (2, "ТРУ, относящихся к инновационной и высокотехнологичной продукции", "ListsInnov")
    ;

    private int id;
    private String name;
    private String code;
}
