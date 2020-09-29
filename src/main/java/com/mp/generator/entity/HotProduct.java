package com.mp.generator.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
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
 * @since 2020-09-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class HotProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 产品链接
     */
    private String productLink;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品价格
     */
    private String productPrice;

    /**
     * 月成交量
     */
    private String monthlyTurnover;

    /**
     * 评论数
     */
    private String numberOfComments;

    /**
     * 采购重复率
     */
    private String procurementRepetitionRate;

    /**
     * 物流发货地
     */
    private String originalDeliverAddr;

    /**
     * 物流运费
     */
    private String logisticFee;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺链接
     */
    private String shopLink;

    /**
     * 店铺所在地区
     */
    private String shopLocation;

    /**
     * 跨境包裹重量
     */
    private String crossBorderWeight;

    /**
     * 产品体积
     */
    private String productVolume;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除
     */
    private Boolean deleted;


}
