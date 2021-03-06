package de.sven_torben.cqrs.domain.events;

import org.apache.commons.lang3.StringUtils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Utility class to implement double dispatch pattern for event handling and consumption.
 *
 * @see IConsumeEvents
 * @see IHandleEvents
 */
public final class EventApplier {

  private EventApplier() {
  }

  /**
   * Calls method wit name {@code methodName} on instance {@code target} and passes the given
   * {@code event}.
   *
   * @param target
   *          Object on which the method {@code methodName} should be called.
   * @param event
   *          Argument to pass to the method.
   * @param methodName
   *          The name of the method.
   */
  public static void apply(Object target, IAmAnEvent event, String methodName) {
    Objects.requireNonNull(target);
    Objects.requireNonNull(event);
    Objects.requireNonNull(StringUtils.trimToNull(methodName));
    try {
      Method method = findHandleMethodInHierarchy(target, event, methodName);
      boolean accesible = method.isAccessible();
      method.setAccessible(true);
      try {
        MethodHandles.lookup().in(target.getClass()).unreflect(method).bindTo(target)
            .invokeWithArguments(event);
      } catch (Throwable e) {
        throw new RuntimeException(e);
      } finally {
        method.setAccessible(accesible);
      }
    } catch (NoSuchMethodException | SecurityException e) {
      throw new RuntimeException(e);
    }
  }

  private static Method findHandleMethodInHierarchy(Object target, IAmAnEvent event,
      String methodName) throws NoSuchMethodException, SecurityException {
    Objects.requireNonNull(target);
    Objects.requireNonNull(event);
    Objects.requireNonNull(StringUtils.trimToNull(methodName));
    Method method = null;
    Class<?> clazz = event.getClass();
    while (method == null && !clazz.equals(Object.class)) {
      try {
        method = findHandleMethod(target, clazz, methodName);
      } catch (NoSuchMethodException | SecurityException e) {
        clazz = clazz.getSuperclass();
      }
    }
    if (method == null) {
      method = findHandleMethod(target, IAmAnEvent.class, methodName);
    }
    return method;
  }

  private static Method findHandleMethod(Object target, Class<?> clazz, String methodName)
      throws NoSuchMethodException, SecurityException {
    Objects.requireNonNull(target);
    Objects.requireNonNull(StringUtils.trimToNull(methodName));
    Method method;
    try {
      method = target.getClass().getMethod(methodName, clazz);
    } catch (NoSuchMethodException | SecurityException e) {
      method = target.getClass().getDeclaredMethod(methodName, clazz);
    }
    return method;
  }
}
