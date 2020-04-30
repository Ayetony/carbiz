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
 * 供应商采集信息表
 * </p>
 *
 * @author ayetony
 * @since 2020-04-30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AlibabaSupplierInfoPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * U_G1/平台
     */
    private String sourceSite;

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
     * U_M1/成立时间
     */
    private String startTime;

    /**
     * U_M2/员工人数
     */
    private String numberOfEmployee;

    /**
     * U_M3/厂房面积
     */
    private String squaresOfStore;

    /**
     * U_P4/主营产品
     */
    private String mainProduct;

    /**
     * U_M4/经营模式
     */
    private String businessType;

    /**
     * U_M5/注册地址
     */
    private String shopLocation;

    /**
     * U_NM1/诚信通年限
     */
    private String cxtYears;

    /**
     * U_M6/卖家信用等级
     */
    private String creditSellerRank;

    /**
     * U_G13/累计销量
     */
    private String numberCummulativeSale;

    /**`
     * U_G14/买家数量
     */
    private String numberOfBuyer;

    /**
     * U_G28/重复采购率
     */
    private String customerRebuyRate;

    /**
     * U_G29/争议率
     */
    private String disputeRate;

    /**
     * U_M15/货描相符
     */
    private String productDescribeCompareWithAverageRate;

    /**
     * U_M16/响应速度
     */
    private String replyTimeCompareWithAverageRate;

    /**
     * U_M17/发货速度
     */
    private String deliverTimeCompareWithAverageRate;

    /**
     * U_M18/回头率
     */
    private String returnRate;

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


}
