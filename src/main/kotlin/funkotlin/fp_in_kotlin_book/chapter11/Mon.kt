package funkotlin.fp_in_kotlin_book.chapter11

import arrow.Kind

interface Mon<F> {
    fun <A, B> flatMap(fa: Kind<F, A>, f: (A) -> Kind<F, B>): Kind<F, B>

    fun <A, B> map(fa: Kind<F, A>, f: (A) -> B): Kind<F, B>

    fun <A, B, C> map2(
        fa: Kind<F, A>,
        fb: Kind<F, B>,
        f: (A, B) -> C,
    ): Kind<F, C> =
        flatMap(fa) { a -> map(fb) { b -> f(a, b) } }

    fun <A> equivalenceOfIdentityLaws(
        m: Monad<F>,
        f: (A) -> Kind<F, A>,
        x: Kind<F, A>,
        v: A,
    ) {
        // expect: "right identity"
        val rightId1: Boolean = m.compose(f, { a: A -> m.unit(a) })(v) == f(v)
        val rightId2: Boolean = { b: A -> m.flatMap(f(b)) { a: A -> m.unit(a) } }(v) == f(v)
        val rightId3: Boolean = m.flatMap(f(v)) { a: A -> m.unit(a) } == f(v)
        val rightId4: Boolean = m.flatMap(x) { a: A -> m.unit(a) } == x

        // expect: "left identity"
        val leftId1: Boolean = m.compose( { a: A -> m.unit(a) }, f)(v) == f(v)
        val leftId2: Boolean = { b: A -> m.flatMap({ a: A -> m.unit(a) }(b), f)  }(v) == f(v)
        val leftId3: Boolean = { b: A -> m.flatMap(m.unit(b), f) }(v) == f(v)
        val leftId4: Boolean = m.flatMap(m.unit(v), f) == f(v)
    }

    fun <A> equivalenceOfIdentityLawsInTermsOfJoinMapUnit(
        m: Monad<F>,
        f: (A) -> Kind<F, A>,
        g: (A) -> Kind<F, A>,
        x: Kind<F, A>, // a appliad to f
        y3: Kind<F, Kind<F, Kind<F, A>>>, // higher kind
        v: A,
    ) {
        // given: id function
        val z3: (Kind<F, Kind<F, A>>) -> Kind<F, Kind<F, Kind<F, A>>> = { a -> m.unit(a) }   // id function

        // expect: "left and right identity laws"
        val line0: Boolean = m.flatMap(f(v)) { a: A -> m.unit(a) } == f(v)
        val line1: Boolean = m.flatMap(x) { a: A -> m.unit(a) } == x
        // and: "replace f and g with id functions"
        val line2: Boolean = m.flatMap(m.flatMap(y3, z3)) { b -> b } ==
                m.flatMap(y3) { a -> m.flatMap(z3(a)){ b -> b } }
        val line3: Boolean = m.flatMap(m.flatMap(y3, z3)) { it } ==
                m.flatMap(y3) { a -> m.flatMap(z3(a)) { it } }
        val line4: Boolean = m.flatMap(m.join(y3)) { it } ==
                m.flatMap(y3) { m.join(it) }
        val line5: Boolean = m.join(m.join(y3)) ==
                m.join(m.map(y3) { m.join(it) })
        val line6: Boolean = m.join(m.unit(x)) ==
                m.join(m.map(x) { m.unit(it) })
    }

}
