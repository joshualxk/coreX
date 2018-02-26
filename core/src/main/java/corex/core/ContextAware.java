package corex.core;

/**
 * Created by Joshua on 2018/3/19.
 */
public interface ContextAware {

    default CoreX coreX() {
        return context().coreX();
    }

    Context context();

    void init(Context context);
}
