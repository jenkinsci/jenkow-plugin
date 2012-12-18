package com.cisco.step.jenkins.plugins.jenkow.identity;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import hudson.security.SecurityRealm;
import jenkins.model.Jenkins;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.interceptor.CommandContext;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * {@link UserQuery} in case Jenkins doesn't have {@link SecurityRealm}.
 *
 * @author Kohsuke Kawaguchi
 */
class AnonymousUserQueryImpl extends UserQueryImpl {
    private final User ANONYMOUS;

    AnonymousUserQueryImpl() {
        ANONYMOUS = new ImmutableUser(Jenkins.ANONYMOUS.getName(),Jenkins.ANONYMOUS.getName(),"","");
    }

    @Override
    public List<User> executeList(CommandContext _, Page page) {
        return query();
    }

    @Override
    public long executeCount(CommandContext _) {
        return executeList(_,null).size();
    }
    
    public List<User> query() {
        return query(Predicates.and(
                checker(new Function<User, String>() {
                    public String apply(User u) {
                        return u.getId();
                    }
                }, literalAndLike(id, null)),

                checker(new Function<User, String>() {
                    public String apply(User u) {
                        return u.getEmail();
                    }
                }, literalAndLike(email, emailLike)),

                checker(new Function<User, String>() {
                    public String apply(User u) {
                        return u.getFirstName();
                    }
                }, literalAndLike(firstName, firstNameLike)),

                checker(new Function<User, String>() {
                    public String apply(User u) {
                        return u.getLastName();
                    }
                }, literalAndLike(lastName, lastNameLike))));
    }

    private Predicate<User> checker(final Function<User,String> function, final Predicate<String> condition) {
        return new Predicate<User>() {
            public boolean apply(User input) {
                return condition.apply(function.apply(input));
            }
        };
    }

    private Predicate<String> literalAndLike(final String exact, String like) {
        if (exact==null && like==null)      return Predicates.alwaysTrue();

        final Pattern p = (like!=null) ? Pattern.compile(like.replace("%",".*").replace('_','?')) : null;
        return new Predicate<String>() {
            public boolean apply(String input) {
                return (exact != null && exact.equals(input))
                    || (p != null && p.matcher(input).matches());
            }
        };
    }

    private List<User> query(Predicate<? super User> pred) {
        if (pred.apply(ANONYMOUS))
            return Collections.singletonList(ANONYMOUS);
        else
            return Collections.emptyList();
    }
}
