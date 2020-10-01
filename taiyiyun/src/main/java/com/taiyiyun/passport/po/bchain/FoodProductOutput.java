package com.taiyiyun.passport.po.bchain;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by zhangjun on 2018/3/26.
 */
public class FoodProductOutput implements Serializable {

    /**产品名称*/
    private String productName;
    /*产品类型*/
    private String productType;
    /*产品数量*/
    private String productCount;
    /*兑换积分*/
    private Integer  points;
    /*产品大约价值（RMB）*/
    private BigDecimal price;
    /*产品描述*/
    private String descreption;
    /*产品详细页url（h5页面）*/
    private String detailUrl;
    /*产品图片url*/
    private String imageUrl;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductCount() {
        return productCount;
    }

    public void setProductCount(String productCount) {
        this.productCount = productCount;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescreption() {
        return descreption;
    }

    public void setDescreption(String descreption) {
        this.descreption = descreption;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
