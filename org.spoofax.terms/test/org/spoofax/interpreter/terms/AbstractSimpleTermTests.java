package org.spoofax.interpreter.terms;


import org.junit.jupiter.api.DisplayName;
import org.spoofax.terms.AbstractSimpleTerm;
import org.spoofax.terms.attachments.ITermAttachment;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;


/**
 * Tests the {@link AbstractSimpleTerm} abstract class.
 */
@DisplayName("AbstractSimpleTerm")
public interface AbstractSimpleTermTests extends ISimpleTermTests {

    /**
     * Creates a new instance of the {@link AbstractSimpleTerm} for testing.
     *
     * @param subterms the subterms of the term; or {@code null} to use a sensible default
     * @param attachments the attachments of the term; or {@code null} to use a sensible default
     * @return the created object; or {@code null} when an instance with the given parameters could not be created
     */
    @Nullable
    AbstractSimpleTerm createAbstractSimpleTerm(@Nullable List<ISimpleTerm> subterms, @Nullable List<ITermAttachment> attachments);

    /**
     * Creates a new instance of the {@link AbstractSimpleTerm} for testing.
     *
     * @param subterms the subterms of the term; or {@code null} to use a sensible default
     * @return the created object; or {@code null} when an instance with the given parameters could not be created
     */
    @Nullable default AbstractSimpleTerm createAbstractSimpleTerm(@Nullable List<ISimpleTerm> subterms) {
        return createAbstractSimpleTerm(subterms, Collections.emptyList());
    }

}
