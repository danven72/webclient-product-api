import dto.Product;
import dto.ProductEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class WebClientAPI {
    private WebClient webClient;

    public WebClientAPI() {
        this.webClient = WebClient.create("http://localhost:8080/products");
    }

    public static void main(String[] args) {
        WebClientAPI api = new WebClientAPI();

        api.postNewProduct()
                .thenMany(api.getAllProducts()) // thanMany wait untill the previos operation is finished
                .take(1)
                .flatMap(p -> api.updateProduct(p.getId(), "White TEA", 1.20))
                .flatMap(p -> api.deleteProduct(p.getId()))
                .thenMany(api.getAllProducts())
                .thenMany(api.getAllEvents())
                .subscribe(System.out::println);

        //The program is asynchronous, so if you don't put a sleep, it ends before
        // the response is arrived
        try {
            Thread.sleep(5000);
        }
        catch(Exception ignore) {

        }
    }

    private Mono<ResponseEntity<Product>> postNewProduct() {
        return webClient
                .post()
                .body(Mono.just(new Product(null, "Jasmine Tea", 1.99)), Product.class)
                .exchangeToMono(response -> response.toEntity(Product.class)) //here perform the request
                .doOnSuccess(o -> System.out.println("********** POST " + o));
    }

    private Flux<Product> getAllProducts() {
        return webClient
                .get()
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnNext(o -> System.out.println("********** GET " + o));
    }

    private Mono<Product> updateProduct(String id, String name, double price) {
        return webClient
                .put()
                .uri("/{id}", id)
                .body(Mono.just(new Product(null, name, price)), Product.class)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnSuccess(o -> System.out.println("********** UPDATE " + o));
    }

    private Mono<Void> deleteProduct(String id) {
        return webClient
                .delete()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(o -> System.out.println("********** DELETE " + o));
    }

    private Flux<ProductEvent> getAllEvents() {
        return webClient
                .get()
                .uri("/events")
                .retrieve()
                .bodyToFlux(ProductEvent.class);
    }
}
