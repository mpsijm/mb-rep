package org.spoofax.interpreter.terms;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.spoofax.terms.TermFactory;
import org.spoofax.terms.attachments.ITermAttachment;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


/**
 * Tests the {@link IStrategoList} interface.
 */
@DisplayName("IStrategoList")
public interface IStrategoListTests extends IStrategoTermTests {

    /**
     * Creates a new instance of the {@link IStrategoList} for testing.
     *
     * @param elements the elements of the list
     * @param annotations the annotations of the term
     * @param attachments the attachments of the term
     * @return the created object; or {@code null} when it could not be created
     */
    @Nullable
    default IStrategoList createStrategoList(List<IStrategoTerm> elements,
                                     IStrategoList annotations, List<ITermAttachment> attachments) {
        if (elements.isEmpty()) { return createStrategoEmptyList(annotations, attachments); }
        return createStrategoList(elements.get(0), createStrategoList(elements.subList(1, elements.size())), annotations, attachments);
    }

    /**
     * Creates a new instance of the {@link IStrategoList} for testing.
     *
     * @param elements the elements of the list
     * @param annotations the annotations of the term
     * @return the created object; or {@code null} when it could not be created
     */
    @Nullable
    default IStrategoList createStrategoList(List<IStrategoTerm> elements, IStrategoList annotations) {
        return createStrategoList(elements, annotations, Collections.emptyList());
    }

    /**
     * Creates a new instance of the {@link IStrategoList} for testing.
     *
     * @param elements the elements of the list
     * @return the created object; or {@code null} when it could not be created
     */
    @Nullable
    default IStrategoList createStrategoList(List<IStrategoTerm> elements) {
        return createStrategoList(elements, TermFactory.EMPTY_LIST);
    }


    /**
     * Creates a new instance of the {@link IStrategoList} for testing.
     *
     * @param head the head of the list
     * @param tail the tail of the list
     * @param annotations the annotations of the term
     * @param attachments the attachments of the term
     * @return the created object; or {@code null} when it could not be created
     */
    @Nullable
    IStrategoList createStrategoList(IStrategoTerm head, IStrategoList tail,
                                     IStrategoList annotations, List<ITermAttachment> attachments);

    /**
     * Creates a new instance of the {@link IStrategoList} for testing.
     *
     * @param head the head of the list
     * @param tail the tail of the list
     * @param annotations the annotations of the term
     * @return the created object; or {@code null} when it could not be created
     */
    @Nullable
    default IStrategoList createStrategoList(IStrategoTerm head, IStrategoList tail, IStrategoList annotations) {
        return createStrategoList(head, tail, annotations, Collections.emptyList());
    }

    /**
     * Creates a new instance of the {@link IStrategoList} for testing.
     *
     * @param head the head of the list
     * @param tail the tail of the list
     * @return the created object; or {@code null} when it could not be created
     */
    @Nullable
    default IStrategoList createStrategoList(IStrategoTerm head, IStrategoList tail) {
        return createStrategoList(head, tail, TermFactory.EMPTY_LIST);
    }

    /**
     * Creates a new instance of the {@link IStrategoList} for testing.
     *
     * This creates an empty list.
     *
     * @param annotations the annotations of the term
     * @param attachments the attachments of the term
     * @return the created object
     */
    IStrategoList createStrategoEmptyList(IStrategoList annotations, List<ITermAttachment> attachments);

    /**
     * Creates a new instance of the {@link IStrategoList} for testing.
     *
     * This creates an empty list.
     *
     * @param annotations the annotations of the term
     * @return the created object
     */
    default IStrategoList createStrategoEmptyList(IStrategoList annotations) {
        return createStrategoEmptyList(annotations, Collections.emptyList());
    }

    /**
     * Creates a new instance of the {@link IStrategoList} for testing.
     *
     * This creates an empty list.
     *
     * @return the created object
     */
    default IStrategoList createStrategoEmptyList() {
        return createStrategoEmptyList(TermFactory.EMPTY_LIST);
    }


    @Nullable
    @Override
    default IStrategoTerm createStrategoTerm(List<IStrategoTerm> subterms, IStrategoList annotations,
                                             List<ITermAttachment> attachments) {
        return createStrategoList(subterms, annotations, attachments);
    }

    /**
     * Tests the {@link IStrategoList#size()} method.
     */
    @DisplayName("size()")
    interface SizeTests extends IStrategoListTests {

        @Test
        @DisplayName("when the list is empty, returns zero")
        default void whenTheListIsEmpty_returnsZero() {
            // Arrange
            IStrategoList sut = createStrategoEmptyList();

            // Act
            int result = sut.size();

            // Assert
            assertEquals(0, result);
        }

        @Test
        @DisplayName("when the list is not empty, returns the size")
        default void whenTheListIsNotEmpty_returnsTheSize() {
            // Arrange
            List<IStrategoTerm> elements = Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm());
            IStrategoList sut = createStrategoList(elements);

            // Assume
            assumeTrue(sut != null, "The implementation does not support the given elements.");

            // Act
            int result = sut.size();

            // Assert
            assertEquals(2, result);
        }

    }

    /**
     * Tests the {@link IStrategoList#head()} method.
     */
    @DisplayName("head()")
    interface HeadTests extends IStrategoListTests {

        @Test
        @DisplayName("when the list is empty, throws exception")
        default void whenTheListIsEmpty_throwsException() {
            // Arrange
            IStrategoList sut = createStrategoEmptyList();

            // Act/Assert
            assertThrows(NoSuchElementException.class, () -> {
                sut.head();
            });
        }

        @Test
        @DisplayName("when the list is not empty, returns the first element")
        default void whenTheListIsNotEmpty_returnsTheFirstElement() {
            // Arrange
            List<IStrategoTerm> elements = Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm());
            IStrategoList sut = createStrategoList(elements);

            // Assume
            assumeTrue(sut != null, "The implementation does not support the given elements.");

            // Act
            IStrategoTerm result = sut.head();

            // Assert
            assertSame(elements.get(0), result);
        }

    }


    /**
     * Tests the {@link IStrategoList#tail()} method.
     */
    @DisplayName("tail()")
    interface TailTests extends IStrategoListTests {

        @Test
        @DisplayName("when the list is empty, throws exception")
        default void whenTheListIsEmpty_throwsException() {
            // Arrange
            IStrategoList sut = createStrategoEmptyList();

            // Act/Assert
            assertThrows(IllegalStateException.class, () -> {
                sut.tail();
            });
        }

        @Test
        @DisplayName("when the list is not empty, returns the tail list")
        default void whenTheListIsNotEmpty_returnsTheTailList() {
            // Arrange
            List<IStrategoTerm> elements = Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm());
            IStrategoList sut = createStrategoList(elements);

            // Assume
            assumeTrue(sut != null, "The implementation does not support the given elements.");

            // Act
            IStrategoList result = sut.tail();

            // Assert
            assertEquals(elements.subList(1, elements.size()), Arrays.asList(result.getAllSubterms()));
        }

        @Test
        @DisplayName("when the list is a singleton list, returns the empty list")
        default void whenTheListIsASingletonList_returnsTheEmptyList() {
            // Arrange
            IStrategoList sut = createStrategoList(Collections.singletonList(new DummyStrategoTerm()));

            // Assume
            assumeTrue(sut != null, "The implementation does not support the given elements.");

            // Act
            IStrategoList result = sut.tail();

            // Assert
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("when tail has annotations, returns the annotations too")
        default void whenTailHasAnnotations_returnsTheAnnotationsToo() {
            // Arrange
            IStrategoList a0 = getTermBuilder().makeList(new DummyStrategoTerm());
            IStrategoList l0 = createStrategoEmptyList(a0);
            IStrategoList a1 = getTermBuilder().makeList(new DummyStrategoTerm());
            IStrategoList l1 = createStrategoList(new DummyStrategoTerm(), l0, a1);
            IStrategoList a2 = getTermBuilder().makeList(new DummyStrategoTerm());
            IStrategoList sut = createStrategoList(new DummyStrategoTerm(), l1, a2);

            // Assume
            assumeTrue(sut != null, "The implementation does not support the given elements.");

            // Act
            IStrategoList result1 = sut.tail();
            IStrategoList result0 = result1.tail();

            // Assert
            assertSame(sut.getAnnotations(), a2);
            assertSame(result1.getAnnotations(), a1);
            assertSame(result0.getAnnotations(), a0);
        }

        @Test
        @DisplayName("when tail has attachments, returns the attachments too")
        default void whenTailHasAttachments_returnsTheAttachmentsToo() {
            // Arrange
            DummyTermAttachmentType<DummyTermAttachment> attachmentType = DummyTermAttachment.Type1;
            ITermAttachment a0 = new DummyTermAttachment(attachmentType);
            IStrategoList l0 = createStrategoEmptyList(TermFactory.EMPTY_LIST, Collections.singletonList(a0));
            ITermAttachment a1 = new DummyTermAttachment(attachmentType);
            IStrategoList l1 = createStrategoList(new DummyStrategoTerm(), l0, TermFactory.EMPTY_LIST, Collections.singletonList(a1));
            ITermAttachment a2 = new DummyTermAttachment(attachmentType);
            IStrategoList sut = createStrategoList(new DummyStrategoTerm(), l1, TermFactory.EMPTY_LIST, Collections.singletonList(a2));

            // Assume
            assumeTrue(sut != null, "The implementation does not support the given elements.");

            // Act
            IStrategoList result1 = sut.tail();
            IStrategoList result0 = result1.tail();

            // Assert
            assertSame(sut.getAttachment(attachmentType), a2);
            assertSame(result1.getAttachment(attachmentType), a1);
            assertSame(result0.getAttachment(attachmentType), a0);
        }

    }


    /**
     * Tests the {@link IStrategoList#isEmpty()} method.
     */
    @DisplayName("isEmpty()")
    interface IsEmptyTests extends IStrategoListTests {

        @Test
        @DisplayName("when the list is empty, returns true")
        default void whenTheListIsEmpty_returnsTrue() {
            // Arrange
            IStrategoList sut = createStrategoEmptyList();

            // Act
            boolean result = sut.isEmpty();

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("when the list is not empty, returns false")
        default void whenTheListIsNotEmpty_returnsFalse() {
            // Arrange
            IStrategoList sut = createStrategoList(Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm()));

            // Assume
            assumeTrue(sut != null, "The implementation does not support the given elements.");

            // Act
            boolean result = sut.isEmpty();

            // Assert
            assertFalse(result);
        }

    }



    /**
     * Tests the {@link IStrategoList#getTermType()} method.
     */
    @DisplayName("getTermType()")
    interface GetTermTypeTests extends IStrategoListTests, IStrategoTermTests.GetTermTypeTests {

        @Test
        @DisplayName("empty list, returns the correct term type")
        default void emptyList_returnsTheCorrectTermType() {
            // Arrange
            IStrategoList sut = createStrategoEmptyList();

            // Act
            int result = sut.getTermType();

            // Assert
            assertEquals(IStrategoTerm.LIST, result);
        }

        @Test
        @DisplayName("non-empty list, returns the correct term type")
        default void nonEmptyList_returnsTheCorrectTermType() {
            // Arrange
            List<IStrategoTerm> elements = Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm());
            IStrategoList sut = createStrategoList(elements);

            // Assume
            assumeTrue(sut != null, "The implementation does not support the given elements.");

            // Act
            int result = sut.getTermType();

            // Assert
            assertEquals(IStrategoTerm.LIST, result);
        }
    }

    /**
     * Tests the {@link IStrategoList#match(IStrategoTerm)} method.
     */
    @DisplayName("match(IStrategoTerm)")
    interface MatchTests extends IStrategoListTests, IStrategoTermTests.MatchTests {

        @Test
        @DisplayName("when both are empty lists, returns true")
        default void whenBothAreEmptyLists_returnsTrue() {
            // Arrange
            List<IStrategoTerm> elements = Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm());
            IStrategoList sut = createStrategoList(elements);
            IStrategoList other = createStrategoList(elements);

            // Assume
            assumeTrue(sut != null && other != null, "The implementation does not support the given constructor and/or subterms.");

            // Act
            boolean result = sut.match(other);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("when both have the same elements, returns true")
        default void whenBothHaveTheSameElements_returnsTrue() {
            // Arrange
            List<IStrategoTerm> elements = Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm());
            IStrategoList sut = createStrategoList(elements);
            IStrategoList other = createStrategoList(elements);

            // Assume
            assumeTrue(sut != null && other != null, "The implementation does not support the given constructor and/or subterms.");

            // Act
            boolean result = sut.match(other);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("when other has differing elements, returns false")
        default void whenOtherHasDifferingElements_returnsFalse() {
            // Arrange
            IStrategoList sut = createStrategoList(Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm()));
            IStrategoList other = createStrategoList(Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm()));

            // Assume
            assumeTrue(sut != null && other != null, "The implementation does not support the given constructor and/or subterms.");

            // Act
            boolean result = sut.match(other);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("when other has different number of elements, returns false")
        default void whenOtherHasDifferentNumberOfElements_returnsFalse() {
            // Arrange
            IStrategoList sut = createStrategoList(Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm()));
            IStrategoList other = createStrategoList(Arrays.asList(new DummyStrategoTerm()));

            // Assume
            assumeTrue(sut != null && other != null, "The implementation does not support the given constructor and/or subterms.");

            // Act
            boolean result = sut.match(other);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("when one is empty, returns false")
        default void whenOneIsEmpty_returnsFalse() {
            // Arrange
            IStrategoList sut = createStrategoList(Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm()));
            IStrategoList other = createStrategoEmptyList();

            // Assume
            assumeTrue(sut != null && other != null, "The implementation does not support the given constructor and/or subterms.");

            // Act
            boolean result = sut.match(other);

            // Assert
            assertFalse(result);
        }

        @Disabled("BUG: Annotations on sublists are not currently compared.")
        @Test
        @DisplayName("when one has different annotations on a sublist, returns false")
        default void whenOneHasDifferentAnnotationsOnASublist_returnsFalse() {
            // Arrange
            IStrategoList anno1 = getTermBuilder().makeList(new DummyStrategoTerm());
            IStrategoList anno2 = getTermBuilder().makeList(new DummyStrategoTerm());
            DummyStrategoTerm t0 = new DummyStrategoTerm();
            DummyStrategoTerm t1 = new DummyStrategoTerm();
            IStrategoList sut = createStrategoList(t1, createStrategoList(t0, createStrategoEmptyList(), anno1));
            IStrategoList other = createStrategoList(t1, createStrategoList(t0, createStrategoEmptyList(), anno2));

            // Assume
            assumeTrue(sut != null && other != null, "The implementation does not support the given constructor and/or subterms.");

            // Act
            boolean result = sut.match(other);

            // Assert
            assertFalse(result);
        }

        @Disabled("BUG: Annotations on sublists are not currently compared.")
        @Test
        @DisplayName("when one has different annotations on the empty list, returns false")
        default void whenOneHasDifferentAnnotationsOnTheEmptyList_returnsFalse() {
            // Arrange
            IStrategoList anno1 = getTermBuilder().makeList(new DummyStrategoTerm());
            IStrategoList anno2 = getTermBuilder().makeList(new DummyStrategoTerm());
            DummyStrategoTerm t0 = new DummyStrategoTerm();
            DummyStrategoTerm t1 = new DummyStrategoTerm();
            IStrategoList sut = createStrategoList(t1, createStrategoList(t0, createStrategoEmptyList(anno1)));
            IStrategoList other = createStrategoList(t1, createStrategoList(t0, createStrategoEmptyList(anno2)));

            // Assume
            assumeTrue(sut != null && other != null, "The implementation does not support the given constructor and/or subterms.");

            // Act
            boolean result = sut.match(other);

            // Assert
            assertFalse(result);
        }

    }


    /**
     * Tests the {@link IStrategoInt#toString(int)} and {@link IStrategoInt#toString()} methods.
     */
    @DisplayName("toString(..)")
    interface ToStringTests extends IStrategoListTests, IStrategoTermTests.ToStringTests {

        @Test
        @DisplayName("returns the correct string representation")
        default void returnsTheCorrectStringRepresentation() {
            // Arrange
            IStrategoList sut = createStrategoList(Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm()));

            // Assume
            assumeTrue(sut != null, "The implementation does not support the given constructor and/or subterms.");

            // Act
            String result = sut.toString();

            // Assert
            assertEquals("[<dummy>,<dummy>]", result);
        }
    }


    /**
     * Tests the {@link IStrategoInt#writeAsString(Appendable, int)} and {@link IStrategoInt#writeAsString(Appendable)} methods.
     */
    @DisplayName("writeAsString(..)")
    interface WriteAsStringTests extends IStrategoListTests, IStrategoTermTests.WriteAsStringTests {

        @Test
        @DisplayName("returns the correct string representation")
        default void returnsTheCorrectStringRepresentation() throws IOException {
            // Arrange
            StringBuilder sb = new StringBuilder();
            IStrategoList sut = createStrategoList(Arrays.asList(new DummyStrategoTerm(), new DummyStrategoTerm()));

            // Assume
            assumeTrue(sut != null, "The implementation does not support the given constructor and/or subterms.");

            // Act
            sut.writeAsString(sb);

            // Assert
            assertEquals("[<dummy>,<dummy>]", sb.toString());
        }

    }
}
