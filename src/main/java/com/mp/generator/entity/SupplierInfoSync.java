package com.mp.generator.entity;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author ayetony
 * @since 2020-05-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SupplierInfoSync implements Serializable {

    private static final long serialVersionUID = 1L;

    private String shopRef;

    private String sellerNick;

    private String companyName;

    private String mainProduct;

    private String businessType;

    private String shopLocation;

    private String totalSold;

    private String keyword;


}
