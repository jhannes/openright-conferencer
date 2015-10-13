package net.openright.conferencer.domain.orders;

import net.openright.conferencer.application.ConferencerConfig;
import net.openright.conferencer.application.ConferencerTestConfig;
import net.openright.conferencer.domain.orders.Order;
import net.openright.conferencer.domain.orders.OrdersRepository;
import net.openright.conferencer.domain.products.Product;
import net.openright.conferencer.domain.products.ProductRepository;
import net.openright.conferencer.domain.products.ProductRepositoryTest;
import net.openright.infrastructure.test.SampleData;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderRepositoryTest {

    private ConferencerConfig config = ConferencerTestConfig.instance();
    private OrdersRepository repository = new OrdersRepository(config);
    private ProductRepository productRepository = new ProductRepository(config);
    private Product product = ProductRepositoryTest.sampleProduct();

    @Before
    public void saveProduct() {
        productRepository.insert(product);
    }

    @Test
    public void shouldRetrieveSavedOrdersWithoutOrderLines() throws Exception {
        Order order = sampleOrder();
        repository.insert(order);
        assertThat(repository.list()).contains(order);

        assertThat(repository.retrieve(order.getId()))
            .isEqualToComparingFieldByField(order);
    }

    @Test
    public void shouldListOrdersWithoutOrderLines() throws Exception {
        Order order = sampleOrder();
        order.addOrderLine(product.getId(), 2);
        repository.insert(order);

        Order savedOrder =  repository.list().stream()
                .filter(o -> o.getId() == order.getId())
                .findFirst().get();
        assertThat(savedOrder.getOrderLines()).isEmpty();
    }

    @Test
    public void shouldIncludeProductWhenRetrievingOrder() throws Exception {
        Order order = sampleOrder();
        order.addOrderLine(product.getId(), 2);
        repository.insert(order);

        Order savedOrder = repository.retrieve(order.getId());
        assertThat(savedOrder.getOrderLines().get(0).getProduct().get())
            .isEqualTo(product);
        assertThat(savedOrder.getOrderLines().get(0).getAmount())
            .isEqualTo(2);
    }

    @Test
    public void shouldUpdateOrder() throws Exception {
        Order order = sampleOrder();
        order.addOrderLine(product.getId(), 2);
        repository.insert(order);

        order.getOrderLines().get(0).setAmount(1000);
        order.setTitle(SampleData.sampleString(3));
        repository.update(order.getId(), order);

        assertThat(repository.retrieve(order.getId()).getTitle())
            .isEqualTo(order.getTitle());
        assertThat(repository.retrieve(order.getId()).getOrderLines().get(0).getAmount())
            .isEqualTo(order.getOrderLines().get(0).getAmount());
    }

    @Test
    public void shouldRetrieveSavedOrdersWithOrderLines() throws Exception {
        Product product2 = ProductRepositoryTest.sampleProduct();
        productRepository.insert(product2);

        Order order = sampleOrder();

        order.addOrderLine(product.getId(), 10);
        order.addOrderLine(product2.getId(), 100);

        repository.insert(order);

        Order savedOrder = repository.retrieve(order.getId());
        assertThat(savedOrder).isEqualToComparingFieldByField(order);
        assertThat(savedOrder.getTotalAmount())
            .isEqualTo(10 * product.getPrice() + 100 * product2.getPrice());
    }

    public static Order sampleOrder() {
        return new Order(SampleData.sampleString(4));
    }

}
