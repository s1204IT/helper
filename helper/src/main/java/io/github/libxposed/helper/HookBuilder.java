package io.github.libxposed.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Map;

import dalvik.system.BaseDexClassLoader;
import io.github.libxposed.api.XposedContextWrapper;

@SuppressWarnings("unused")
public interface HookBuilder {
    @FunctionalInterface
    interface Consumer<T> {
        void accept(@NonNull T t);
    }

    @FunctionalInterface
    interface Predicate<T> {
        boolean test(@NonNull T t);
    }

    @FunctionalInterface
    interface MatchConsumer<T, U> {
        @NonNull
        U accept(@NonNull T t);
    }

    @NonNull
    static MatchResult buildHook(@NonNull XposedContextWrapper ctx, @NonNull BaseDexClassLoader classLoader, @NonNull java.lang.String sourcePath, Consumer<HookBuilder> consumer) {
        var builder = new HookBuilderImpl(ctx, classLoader, sourcePath);
        consumer.accept(builder);
        return builder.build();
    }

    interface MatchResult extends Serializable {
        @NonNull
        Map<java.lang.String, java.lang.Class<?>> getMatchedClasses();

        @NonNull
        Map<java.lang.String, java.lang.reflect.Field> getMatchedFields();

        @NonNull
        Map<java.lang.String, java.lang.reflect.Method> getMatchedMethods();

        @NonNull
        Map<java.lang.String, java.lang.reflect.Constructor<?>> getMatchedConstructors();
    }

    interface BaseMatcher<Self extends BaseMatcher<Self, Match>, Match extends BaseMatch<Match, ?>> {
        @NonNull
        Self setMatchFirst(boolean matchFirst);

        @NonNull
        Self setMissReplacement(@NonNull Match replacement);
    }

    interface ReflectMatcher<Self extends ReflectMatcher<Self, Match>, Match extends ReflectMatch<Match, ?>> extends BaseMatcher<Self, Match> {
        Self setKey(@NonNull java.lang.String key);

        Self setIsPublic(boolean isPublic);

        Self setIsPrivate(boolean isPrivate);

        Self setIsProtected(boolean isProtected);

        Self setIsPackage(boolean isPackage);
    }

    interface ContainerSyntax<Match extends BaseMatch<Match, ?>> {
        @NonNull
        ContainerSyntax<Match> and(@NonNull Match element);

        @NonNull
        ContainerSyntax<Match> and(@NonNull ContainerSyntax<Match> predicate);

        @NonNull
        ContainerSyntax<Match> or(@NonNull Match element);

        @NonNull
        ContainerSyntax<Match> or(@NonNull ContainerSyntax<Match> predicate);

        @NonNull
        ContainerSyntax<Match> not();
    }

    interface TypeMatcher<Self extends TypeMatcher<Self, Match>, Match extends TypeMatch<Match>> extends ReflectMatcher<Self, Match> {
        @NonNull
        Self setName(@NonNull String name);

        @NonNull
        Self setSuperClass(@NonNull Class superClass);

        @NonNull
        Self setContainsMethods(@NonNull ContainerSyntax<Method> syntax);

        @NonNull
        Self setContainsConstructors(@NonNull ContainerSyntax<Constructor> syntax);

        @NonNull
        Self setContainsFields(@NonNull ContainerSyntax<Field> syntax);

        @NonNull
        Self setInterfaces(@NonNull ContainerSyntax<Class> syntax);

        @NonNull
        Self setIsAbstract(boolean isAbstract);

        @NonNull
        Self setIsStatic(boolean isStatic);

        @NonNull
        Self setIsFinal(boolean isFinal);
    }

    interface ClassMatcher extends TypeMatcher<ClassMatcher, Class> {
    }

    interface ParameterMatcher extends TypeMatcher<ParameterMatcher, Parameter> {
        @NonNull
        ParameterMatcher setIndex(int index);
    }

    interface StringMatcher extends BaseMatcher<StringMatcher, String> {
        @NonNull
        StringMatcher setExact(@NonNull java.lang.String exact);

        @NonNull
        StringMatcher setPrefix(@NonNull java.lang.String prefix);
    }

    interface MemberMatcher<Self extends MemberMatcher<Self, Match>, Match extends MemberMatch<Match, ?>> extends ReflectMatcher<Self, Match> {
        @NonNull
        Self setDeclaringClass(@NonNull Class declaringClass);

        @NonNull
        Self setIsSynthetic(boolean isSynthetic);
    }

    interface FieldMatcher extends MemberMatcher<FieldMatcher, Field> {
        @NonNull
        FieldMatcher setName(@NonNull String name);

        @NonNull
        FieldMatcher setType(@NonNull Class type);

        @NonNull
        FieldMatcher setIsStatic(boolean isStatic);

        @NonNull
        FieldMatcher setIsFinal(boolean isFinal);

        @NonNull
        FieldMatcher setIsTransient(boolean isTransient);

        @NonNull
        FieldMatcher setIsVolatile(boolean isVolatile);
    }

    interface ExecutableMatcher<Self extends ExecutableMatcher<Self, Match>, Match extends ExecutableMatch<Match, ?>> extends MemberMatcher<Self, Match> {
        @NonNull
        Self setParameterCount(int count);

        @NonNull
        Self setParameterTypes(@NonNull ContainerSyntax<Parameter> parameterTypes);

        @NonNull
        Self setReferredStrings(@NonNull ContainerSyntax<String> referredStrings);

        @NonNull
        Self setAssignedFields(@NonNull ContainerSyntax<Field> assignedFields);

        @NonNull
        Self setAccessedFields(@NonNull ContainerSyntax<Field> assignedFields);

        @NonNull
        Self setInvokedMethods(@NonNull ContainerSyntax<Method> invokedMethods);

        @NonNull
        Self setInvokedConstructors(@NonNull ContainerSyntax<Constructor> invokedConstructors);

        @NonNull
        Self setContainsOpcodes(@NonNull Byte[] opcodes);

        @NonNull
        Self setIsVarargs(boolean isVarargs);
    }

    interface MethodMatcher extends ExecutableMatcher<MethodMatcher, Method> {
        @NonNull
        MethodMatcher setName(@NonNull String name);

        @NonNull
        MethodMatcher setReturnType(@NonNull Class returnType);

        @NonNull
        MethodMatcher setIsAbstract(boolean isAbstract);

        @NonNull
        MethodMatcher setIsStatic(boolean isStatic);

        @NonNull
        MethodMatcher setIsFinal(boolean isFinal);

        @NonNull
        MethodMatcher setIsSynchronized(boolean isSynchronized);

        @NonNull
        MethodMatcher setIsNative(boolean isNative);
    }

    interface ConstructorMatcher extends ExecutableMatcher<ConstructorMatcher, Constructor> {
    }

    interface BaseMatch<Self extends BaseMatch<Self, Reflect>, Reflect> {
        @NonNull
        ContainerSyntax<Self> observe();

        @NonNull
        ContainerSyntax<Self> reverse();
    }

    interface ReflectMatch<Self extends ReflectMatch<Self, Reflect>, Reflect> extends BaseMatch<Self, Reflect> {
        @Nullable
        java.lang.String getKey();

        @NonNull
        Self onMatch(@NonNull Consumer<Reflect> consumer);
    }

    interface LazySequence<Match extends BaseMatch<Match, Reflect>, Reflect, Matcher extends BaseMatcher<Matcher, Match>> {
        @NonNull
        Match first();

        @NonNull
        Match first(@NonNull Consumer<Matcher> consumer);

        @NonNull
        LazySequence<Match, Reflect, Matcher> all(@NonNull Consumer<Matcher> consumer);

        @NonNull
        LazySequence<Match, Reflect, Matcher> onMatch(@NonNull Consumer<Iterable<Reflect>> consumer);

        @NonNull
        Match onMatch(MatchConsumer<Iterable<Reflect>, Reflect> consumer);

        @NonNull
        ContainerSyntax<Match> conjunction();

        @NonNull
        ContainerSyntax<Match> disjunction();
    }

    interface TypeMatch<Self extends TypeMatch<Self>> extends ReflectMatch<Self, java.lang.Class<?>> {
        @NonNull
        String getName();

        @NonNull
        Class getSuperClass();

        @NonNull
        LazySequence<Class, java.lang.Class<?>, ClassMatcher> getInterfaces();

        @NonNull
        LazySequence<Method, java.lang.reflect.Method, MethodMatcher> getDeclaredMethods();

        @NonNull
        LazySequence<Constructor, java.lang.reflect.Constructor<?>, ConstructorMatcher> getDeclaredConstructors();

        @NonNull
        LazySequence<Field, java.lang.reflect.Field, FieldMatcher> getDeclaredFields();

        @NonNull
        Class getArrayType();
    }

    interface Class extends TypeMatch<Class> {
        @NonNull
        Parameter asParameter(int index);
    }

    interface Parameter extends TypeMatch<Parameter> {
    }

    interface MemberMatch<Self extends MemberMatch<Self, Reflect>, Reflect extends java.lang.reflect.Member> extends ReflectMatch<Self, Reflect> {
        @NonNull
        Class getDeclaringClass();
    }

    interface ExecutableMatch<Self extends ExecutableMatch<Self, Reflect>, Reflect extends java.lang.reflect.Member> extends MemberMatch<Self, Reflect> {
        @NonNull
        LazySequence<Parameter, java.lang.Class<?>, ParameterMatcher> getParameterTypes();

        @NonNull
        LazySequence<String, java.lang.String, StringMatcher> getReferredStrings();

        @NonNull
        LazySequence<Field, java.lang.reflect.Field, FieldMatcher> getAssignedFields();

        @NonNull
        LazySequence<Field, java.lang.reflect.Field, FieldMatcher> getAccessedFields();

        @NonNull
        LazySequence<Method, java.lang.reflect.Method, MethodMatcher> getInvokedMethods();

        @NonNull
        LazySequence<Constructor, java.lang.reflect.Constructor<?>, ConstructorMatcher> getInvokedConstructors();
    }

    interface Method extends ExecutableMatch<Method, java.lang.reflect.Method> {
        @NonNull
        String getName();

        @NonNull
        Class getReturnType();
    }

    interface Constructor extends ExecutableMatch<Constructor, java.lang.reflect.Constructor<?>> {
    }

    interface Field extends MemberMatch<Field, java.lang.reflect.Field> {
        @NonNull
        String getName();

        @NonNull
        Class getType();
    }

    interface String extends BaseMatch<String, java.lang.String> {

    }

    @NonNull
    HookBuilder setLastMatchResult(@NonNull MatchResult preferenceName);

    @NonNull
    HookBuilder setExceptionHandler(@NonNull Predicate<Throwable> handler);

    @NonNull
    LazySequence<Method, java.lang.reflect.Method, MethodMatcher> methods(@NonNull Consumer<MethodMatcher> matcher);

    @NonNull
    Method firstMethod(@NonNull Consumer<MethodMatcher> matcher);

    @NonNull
    LazySequence<Constructor, java.lang.reflect.Constructor<?>, ConstructorMatcher> constructors(@NonNull Consumer<ConstructorMatcher> matcher);

    @NonNull
    Constructor firstConstructor(@NonNull Consumer<ConstructorMatcher> matcher);

    @NonNull
    LazySequence<Field, java.lang.reflect.Field, FieldMatcher> fields(@NonNull Consumer<FieldMatcher> matcher);

    @NonNull
    Field firstField(@NonNull Consumer<FieldMatcher> matcher);

    @NonNull
    LazySequence<Class, java.lang.Class<?>, ClassMatcher> classes(@NonNull Consumer<ClassMatcher> matcher);

    @NonNull
    Class firstClass(@NonNull Consumer<ClassMatcher> matcher);

    @NonNull
    String string(@NonNull Consumer<StringMatcher> matcher);

    @NonNull
    String exact(@NonNull java.lang.String string);

    @NonNull
    String prefix(@NonNull java.lang.String prefix);

    @NonNull
    Class exactClass(@NonNull java.lang.String name);

    @NonNull
    Class exact(@NonNull java.lang.Class<?> clazz);

    @NonNull
    Method exact(@NonNull java.lang.reflect.Method method);

    @NonNull
    Constructor exact(@NonNull java.lang.reflect.Constructor<?> constructor);

    @NonNull
    Field exact(@NonNull java.lang.reflect.Field field);
}
