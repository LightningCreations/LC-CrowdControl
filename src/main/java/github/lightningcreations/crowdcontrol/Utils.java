package github.lightningcreations.crowdcontrol;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Utils {
    public static <U> Consumer<U> doWhile(Predicate<? super U> condition, Consumer<? super U> accepter){
        return u->{
            while(condition.test(u))
                accepter.accept(u);
        };
    }
}
