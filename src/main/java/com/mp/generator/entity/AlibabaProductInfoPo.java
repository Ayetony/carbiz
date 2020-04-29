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
 * 产品采集信息表
 * </p>
 *
 * @author ayetony
 * @since 2020-04-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AlibabaProductInfoPo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * U_G1/平台
     */
    private String sourceSite;

    /**
     * U_NP1/父级分类
     */
    private String parentCatalog;

    /**
     * U_NP2/子级分类
     */
    private String childCatalog;

    /**
     * U_G2/库存单位(SKU)
     */
    private String sku;

    /**
     * U_P2/商品ID
     */
    private String productIDInSourceSite;

    /**
     * U_P3/商品链接
     */
    private String productRef;

    /**
     * U_NP3/视频链接
     */
    private String videoLink;

    /**
     * U_NP4/图片链接
     */
    private String productImgLink;

    /**
     * U_P4/商品标题
     */
    private String productName;

    /**
     * U_NP5/跨境属性
     */
    private String crossBorderPro;

    /**
     * U_P9/商品详情
     */
    private String productDetail;

    /**
     * U_P10/现价(批发价)
     */
    private String currentPrice;

    /**
     * U_G4/店铺ID
     */
    private String shopID;

    /**
     * U_G5/店铺名称
     */
    private String shopName;

    /**
     * U_G6/店铺链接
     */
    private String shopRef;

    /**
     * U_G7/发货地址
     */
    private String deliveryAddress;

    /**
     * U_G9/快递费
     */
    private String fastShippingFee;

    /**
     * U_G12/本月总销量
     */
    private String totalSaleThisMonth;

    /**
     * U_G13/累计销量
     */
    private String numberCummulativeSale;

    /**
     * U_G14/买家数量
     */
    private String numberOfBuyer;

    /**
     * U_NP6/人均采购数
     */
    private String numberPurchasedPer;

    /**
     * U_G17/评价量
     */
    private String numberOfReview;

    /**
     * U_NP7/评论链接
     */
    private String commentLink;

    /**
     * U_NP8/尺码价格
     */
    private String sizePriceStock;

    /**
     * U_G28/客户回购率
     */
    private String customerRebuyRate;

    /**
     * 动作时间
     */
    private String crawlTime;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;

    /**
     * 物理删除开关 true 删除 false 不删除 默认为false
     */
    private Boolean deletedFlag;

    /**
     * 当前搜索关键字
     */
    private String keyword;

    /**
     * 品牌
     */
    private String brand;
    /**
     * 爬虫类型id
     */
    private Integer crawlId;

    /**
     * 爬虫地址
     */
    private String crawlLink;


}
