package com.cisco.step.jenkins.plugins.jenkow.identity;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import hudson.security.SecurityRealm;
import hudson.tasks.Mailer.UserProperty;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * {@link UserQuery} implementation backed by Jenkins.
 *
 * TODO: with cooperative {@link SecurityRealm} we can implement this more thoroughly.
 *
 * @author Kohsuke Kawaguchi
 */
class JenkowUserQueryImpl extends UserQueryImpl {
    private final SecurityRealm sr;

    JenkowUserQueryImpl(SecurityRealm sr) {
        this.sr = sr;
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
        if (id!=null) {
            try {
                UserDetails d = sr.loadUserByUsername(id);
                if (d!=null)
                    return Collections.singletonList($(d));
            } catch (UsernameNotFoundException e) {
                // no such user
            } catch (DataAccessException e) {
                LOGGER.log(Level.WARNING, "Failed to retrieve user info", e);
            }

            hudson.model.User u = hudson.model.User.get(id, false);
            if (u!=null)
                return Collections.singletonList($(u));
            else
                return Collections.emptyList();
        }

        return query(Predicates.and(
                checker(new Function<hudson.model.User, String>() {
                    public String apply(hudson.model.User u) {
                        UserProperty m = u.getProperty(UserProperty.class);
                        return m != null ? m.getAddress() : "";
                    }
                }, literalAndLike(email, emailLike)),

                checker(new Function<hudson.model.User, String>() {
                    public String apply(hudson.model.User u) {
                        String f = u.getFullName();
                        int idx = f.lastIndexOf(' ');
                        if (idx > 0) return f.substring(0, idx);
                        else return f;
                    }
                }, literalAndLike(firstName, firstNameLike)),

                checker(new Function<hudson.model.User, String>() {
                    public String apply(hudson.model.User u) {
                        String f = u.getFullName();
                        int idx = f.lastIndexOf(' ');
                        if (idx > 0) return f.substring(idx);
                        else return f;
                    }
                }, literalAndLike(lastName, lastNameLike))));
    }

    private Predicate<hudson.model.User> checker(final Function<hudson.model.User,String> function, final Predicate<String> condition) {
        return new Predicate<hudson.model.User>() {
            public boolean apply(hudson.model.User input) {
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

    private List<User> query(Predicate<? super hudson.model.User> pred) {
        List<User> r = new ArrayList<User>();
        for (hudson.model.User u : hudson.model.User.getAll())
            if (pred.apply(u))
                r.add($(u));
        return r;
    }


    private User $(hudson.model.User u) {
        return new ImmutableUser(u);
    }

    private User $(UserDetails d) {
        return new ImmutableUser(
                d.getUsername(), d.getUsername(), null, null);
    }

    private static final Logger LOGGER = Logger.getLogger(JenkowUserQueryImpl.class.getName());
}
