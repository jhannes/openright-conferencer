package net.openright.conferencer.domain.products;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.infrastructure.rest.ResourceApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class ProductsApiController implements ResourceApi {

    private ProductRepository repository;

    public ProductsApiController(ConferencerConfig config) {
        this.repository = new ProductRepository(config);
    }

    @Override
    @Nonnull
    public JSONObject listResources() {
        return new JSONObject()
            .put("products", mapToJSON(repository.list(), this::toJSON));
    }

    @Override
    @Nonnull
    public JSONObject getResource(@Nonnull String id) {
        return toJSON(repository.retrieve(Long.valueOf(id)));
    }

    @Override
    @Nonnull
    public String createResource(@Nonnull JSONObject jsonObject) {
        Product product = toProduct(jsonObject);
        repository.insert(product);
        return String.valueOf(product.getId());
    }

    @Override
    public void updateResource(@Nonnull String id, JSONObject jsonObject) {
        repository.update(Long.valueOf(id), toProduct(jsonObject));
    }

    private <T> JSONArray mapToJSON(List<T> list, Function<T, JSONObject> mapper) {
        return new JSONArray(list.stream().map(mapper).collect(Collectors.toList()));
    }

    private JSONObject toJSON(Product product) {
        return new JSONObject()
            .put("id", product.getId())
            .put("title", product.getTitle())
            .put("price", product.getPrice())
            .put("description", product.getDescription());
    }

    private Product toProduct(JSONObject jsonObject) {
        Product product = new Product();
        product.setTitle(jsonObject.getString("title"));
        product.setPrice(jsonObject.getDouble("price"));
        product.setDescription(jsonObject.getString("description"));
        return product;
    }

}
