package com.mp.generator.entity;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class ProductInfoSync implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "product_id", type = IdType.ASSIGN_ID)
    private String productId;

    private String productRef;

    private String productName;

    private String productImgLink;

    private String shopId;

    private String shopRef;

    private String shopName;

    private Integer totalSaleThisMonth;

    private Integer currentPrice;

    private String customerRebuyRate;

    private String keyword;

    private Integer loyalYears;

    private LocalDateTime updateTime;

    private Integer updateTimes;


}
