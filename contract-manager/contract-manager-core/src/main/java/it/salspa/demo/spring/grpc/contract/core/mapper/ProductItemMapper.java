package it.salspa.demo.spring.grpc.contract.core.mapper;

import it.salspa.demo.spring.grpc.contract.api.Product;
import it.salspa.demo.spring.grpc.contract.core.entity.ProductItemEntity;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED)
public interface ProductItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "contractCode", source = "contractCode")
    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "pricingId", source = "product.pricingId")
    @Mapping(target = "usageAmount", source = "product.usageAmount")
    ProductItemEntity toEntity(Product product, String contractCode);

    default List<ProductItemEntity> toEntityList(List<Product> productsList, String contractCode) {
        if (Objects.isNull(productsList)) return null;

        return productsList.stream()
                .map(product -> toEntity(product, contractCode))
                .toList();
    }

    @Mapping(target = "productId", source = "productId") // TODO: in futuro chiamare il servizio Product per avere i dettagli
    @Mapping(target = "pricingId", source = "pricingId") // TODO: in futuro chiamare il servizio Pricing per avere i dettagli
    @Mapping(target = "usageAmount", source = "usageAmount")
    Product toDto(ProductItemEntity entity);

}
