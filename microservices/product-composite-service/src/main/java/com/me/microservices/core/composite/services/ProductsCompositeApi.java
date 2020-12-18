package com.me.microservices.core.composite.services;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;

import com.me.microservices.core.productcomposite.api.model.HttpErrorInfo;
import com.me.microservices.core.productcomposite.api.model.ProductAggregate;
import com.me.microservices.core.productcomposite.api.model.ProductComposite;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import reactor.core.publisher.Mono;
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2020-12-17T16:38:45.270923+01:00[Europe/Paris]")

@Validated
@Api(value = "products-composite", description = "the products-composite API")
public interface ProductsCompositeApi {

    /**
     * POST /products-composite : Create a product-composite.
     *
     * @param productComposite  (required)
     * @return Not Found (status code 404)
     *         or Unprocessable Entity (status code 422)
     *         or Created (status code 201)
     */
    @ApiOperation(value = "Create a product-composite.", nickname = "createCompositeProduct", notes = "", response = ProductComposite.class, tags={ "product-composite-service-impl", })
    @ApiResponses(value = { 
        @ApiResponse(code = 404, message = "Not Found", response = HttpErrorInfo.class),
        @ApiResponse(code = 422, message = "Unprocessable Entity", response = HttpErrorInfo.class),
        @ApiResponse(code = 201, message = "Created", response = ProductComposite.class) })
    @RequestMapping(value = "/products-composite",
        produces = { "application/json" }, 
        consumes = { "application/json" },
        method = RequestMethod.POST)
    Mono<ResponseEntity<ProductComposite>> createCompositeProduct(@ApiParam(value = "" ,required=true )  @Valid @RequestBody ProductComposite productComposite, ServerWebExchange exchange);


    /**
     * DELETE /products-composite/{productId} : Delete a product-composite.
     *
     * @param productId Product identifier. (required)
     * @return Not Found (status code 404)
     *         or Unprocessable Entity (status code 422)
     *         or OK (status code 200)
     */
    @ApiOperation(value = "Delete a product-composite.", nickname = "deleteCompositeProduct", notes = "", tags={ "product-composite-service-impl", })
    @ApiResponses(value = { 
        @ApiResponse(code = 404, message = "Not Found", response = HttpErrorInfo.class),
        @ApiResponse(code = 422, message = "Unprocessable Entity", response = HttpErrorInfo.class),
        @ApiResponse(code = 200, message = "OK") })
    @RequestMapping(value = "/products-composite/{productId}",
        produces = { "application/json" }, 
        method = RequestMethod.DELETE)
    Mono<ResponseEntity<Void>> deleteCompositeProduct(@ApiParam(value = "Product identifier.",required=true) @PathVariable("productId") Integer productId, ServerWebExchange exchange);


    /**
     * GET /products-composite/{productId} : Get a product-composite.
     *
     * @param productId Product identifier. (required)
     * @param pageNumber Page number. (optional)
     * @param pageSize Page size. (optional)
     * @return Not Found (status code 404)
     *         or Unprocessable Entity (status code 422)
     *         or OK (status code 200)
     */
    @ApiOperation(value = "Get a product-composite.", nickname = "getCompositeProduct", notes = "", response = ProductAggregate.class, tags={ "product-composite-service-impl", })
    @ApiResponses(value = { 
        @ApiResponse(code = 404, message = "Not Found", response = HttpErrorInfo.class),
        @ApiResponse(code = 422, message = "Unprocessable Entity", response = HttpErrorInfo.class),
        @ApiResponse(code = 200, message = "OK", response = ProductAggregate.class) })
    @RequestMapping(value = "/products-composite/{productId}",
        produces = { "application/json" }, 
        method = RequestMethod.GET)
    Mono<ResponseEntity<ProductAggregate>> getCompositeProduct(@ApiParam(value = "Product identifier.",required=true) @PathVariable("productId") Integer productId,@ApiParam(value = "Page number.") @Valid @RequestParam(value = "pageNumber", required = false) Integer pageNumber,@ApiParam(value = "Page size.") @Valid @RequestParam(value = "pageSize", required = false) Integer pageSize, ServerWebExchange exchange);

}
