package learn.spring.reactor;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

public class Reactor101 {

    public static void main(String[] args) {
        flux101();
        mono101();
        map101();
    }

    public static void mono101() {
        Mono<String> noData = Mono.empty();
        noData.subscribe(i -> System.out.println(String.format("noData, Value: %s", i)));

        Mono<String> data = Mono.just("foo");
        data.subscribe(i -> System.out.println(String.format("data, Value: %s", i)));
    }

    public static void flux101() {
        Flux<String> seq1 = Flux.just("foo", "bar", "foobar");
        seq1.subscribe(str -> System.out.println(String.format("seq1, Str: %s", str)));

        List<String> iterable = Arrays.asList("foo", "bar", "foobar");
        Flux<String> seq2 = Flux.fromIterable(iterable);
        seq2.subscribe(str -> System.out.println(String.format("seq2, Str: %s", str)));

        Flux<Integer> numbersFromFiveToSeven = Flux.range(5, 3);
        numbersFromFiveToSeven.subscribe(num -> System.out.println(String.format("numbersFromFiveToSeven, Num: %s", num)));
    }

    public static void map101() {
        Flux<Integer> numbers = Flux.range(1, 5);
        Flux<String> numStr = numbers.map(i -> {
            String str = String.format("num-%s", i);
            if (i <= 3)
                return str;
            throw new RuntimeException("Value greater than 3");
        });
        numStr.subscribe(str -> System.out.println(String.format("numStr, %s", str)),
                error -> System.err.println(String.format("numStr, Error: %s", error)),
                () -> System.out.println("numStr, Done"));
    }
}
