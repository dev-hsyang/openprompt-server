package com.openpromt.coffeee.swf2023.openpromtserver.product.controller;

import com.openpromt.coffeee.swf2023.openpromtserver.product.dto.GetProductDetailResponse;
import com.openpromt.coffeee.swf2023.openpromtserver.product.dto.GetProductListResponse;
import com.openpromt.coffeee.swf2023.openpromtserver.product.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;


@Slf4j
@RequestMapping("/api/v2/product")
@RequiredArgsConstructor
@RestController
@Api("ProductApi : GetProductList, GetProductDetail, BuyCopyright, BuyTicket")
public class ProductApiController {


    private final ProductService productService;

    @ApiOperation(value = "PLP", notes = "Product-type을 parameter로 받아 리스트 리턴")
    @GetMapping("/")
    public List<GetProductListResponse> getProductListByProductType(@RequestParam("product-type") String product_type){

       return productService.getProductListByProductType(product_type);
    }

    @ApiOperation(value = "PDP", notes = "ProductId를 Pathvariable로 입력받아 detail 리턴")
    @GetMapping("/{productId}")
    public GetProductDetailResponse getProductDetail(@PathVariable("productId") Long product_id){
        return productService.getProductDetail(product_id);
    }

    @ApiOperation(value = "저작권 구매", notes = "product_id를 parameter로 입력받아 저작권 구매(cid 리턴)")
    @PatchMapping("/copyright")
    public Long buyCopyright(Principal principal, @RequestParam Long product_id){
        return productService.buyCopyright(product_id,principal.getName());
    }

    @PostMapping("/ticket")
    @ApiOperation(value = "사용권 구매", notes = "product_id를 parameter로 입력받아 사용권 구매(리턴 x)")
    public ResponseEntity<?> buyTicket(Principal principal, @RequestParam Long product_id){
        productService.buyTicket(product_id,principal.getName());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
    /**
     * 사용권, 저작권 판매 등록 필요
     */
}
