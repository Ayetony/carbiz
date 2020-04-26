package com.mp.generator.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @since 2020-04-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ProductInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String productRef;

    private String shopRef;

    private String shopName;

    private String productName;

    private String totalSaleThisMonth;

    @TableField("product_Img_link")
    private String productImgLink;

    private String keyword;

    private String loyalYears;

    private String productId;

    private String shopId;

    private Integer currentPrice;

    private String customerRebuyRate;

    private LocalDateTime updateTime;


}
