package com.mp.generator.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author ayetony
 * @since 2020-10-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class HotCrossborderProduct implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 缩略图
     */
    private String thumbnail;

    /**
     * 标题
     */
    private String title;

    /**
     * 产品详情链接
     */
    private String productLink;

    /**
     * 排名
     */
    private String ranking;

    /**
     * 价格
     */
    private String price;

    /**
     * 创建日期
     */
    private String createDate;

    /**
     * 标签
     */
    private String tags;

    /**
     * 类目
     */
    private String category;

    private LocalDateTime createTime;

    private Boolean deleted;


}
