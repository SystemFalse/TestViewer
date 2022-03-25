package users;

import org.data_transfer.util.*;
import org.data_transfer.util.Package;
import test.Test;
import test.TestManager;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TestInfo {

    private record UserResult(User user, int[] scores, Date date, boolean interrupted) {

        public Package pack() {
            Package pack = new Package();
            pack.put("user_name", user.getName(), StringTransfer.INSTANCE);
            Array<Integer> scores = new Array<>(IntTransfer.INSTANCE);
            for (int score : this.scores) {
                scores.add(score);
            }
            pack.put("scores", scores, new ArrayTransfer<>());
            pack.put("date", date.getTime(), LongTransfer.INSTANCE);
            pack.put("interrupted", interrupted, BooleanTransfer.INSTANCE);
            return pack;
        }

        public static UserResult unpack(Package pack) {
            String userName = pack.getString("user_name");
            User user;
            if (UserSystem.getInstance().hasName(userName)) {
                user = UserSystem.getInstance().programAccess(userName);
            } else {
                user = null;
            }
            if (user == null) {
                throw new RuntimeException("Пользователь с логином \"" + userName + "\" был переименован или удалён");
            }
            Array<Integer> scores = pack.getArray("scores");
            int[] sc = new int[scores.size()];
            for (int i = 0; i < sc.length; i++) {
                sc[i] = scores.get(i);
            }
            Date date = new Date(pack.getLong("date"));
            boolean interrupted = pack.getBoolean("interrupted");
            return new UserResult(user, sc, date, interrupted);
        }
    }

    private final Test test;
    private final ArrayList<UserResult> results;

    public TestInfo(Test test) {
        this.test = test;
        results = new ArrayList<>();
    }

    public Test getTest() {
        return test;
    }

    public void setResult(User user, int[] scores, boolean interrupted) {
        setResult(user, scores, new Date(), interrupted);
    }

    public void setResult(User user, int[] scores, Date date, boolean interrupted) {
        if (user == null || scores == null || date == null) {
            throw new NullPointerException();
        }
        if (scores.length != test.questions()) {
            throw new IllegalArgumentException("Количество полученных баллов за ответы не равно количеству вопросов");
        }
        if (hasResultOf(user.getName())) {
            int index = find(user);
            results.set(index, new UserResult(user, scores, date, interrupted));
        } else {
            results.add(new UserResult(user, scores, date, interrupted));
        }
        TestManager.addToRewrite(this);
    }

    public void removeResult(User user) {
        if (user == null) {
            throw new NullPointerException();
        }
        for (UserResult ur : results) {
            if (ur.user.equals(user)) {
                results.remove(ur);
                TestManager.addToRewrite(this);
                break;
            }
        }
    }

    public boolean hasResultOf(String userName) {
        for (UserResult ur : results) {
            if (ur.user.getName().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    private int find(User user) {
        for (int i = 0; i < results.size(); i++) {
            UserResult ur = results.get(i);
            if (ur.user.equals(user)) {
                return i;
            }
        }
        return -1;
    }

    public int[] getResultsOf(String userName) {
        for (UserResult ur : results) {
            if (ur.user.getName().equals(userName)) {
                return ur.scores;
            }
        }
        throw new NoSuchElementException();
    }

    public int getTotalScore(String userName) {
        int[] scores = getResultsOf(userName);
        AtomicInteger total = new AtomicInteger();
        for (int sc : scores) {
            total.addAndGet(sc);
        }
        return total.get();
    }

    public Date getDateOf(String userName) {
        for (UserResult ur : results) {
            if (ur.user.getName().equals(userName)) {
                return ur.date;
            }
        }
        throw new NoSuchElementException();
    }

    public boolean getInterrupted(String userName) {
        for (UserResult ur : results) {
            if (ur.user.getName().equals(userName)) {
                return ur.interrupted;
            }
        }
        throw new NoSuchElementException();
    }

    public int getNumberOfPassed() {
        return results.size();
    }

    public String[] getPassedUsers() {
        String[] users = new String[results.size()];
        for (int i = 0; i < users.length; i++) {
            users[i] = results.get(i).user.getName();
        }
        return users;
    }

    public void compareWith(TestInfo info) {
        for (UserResult ur : info.results) {
            if (hasResultOf(ur.user.getName())) {
                Date preDate = ur.date;
                Date curDate = getDateOf(ur.user.getName());
                if (preDate.before(curDate)) {
                    removeResult(ur.user);
                    results.add(new UserResult(ur.user, ur.scores, ur.date, ur.interrupted));
                }
            } else {
                results.add(ur);
            }
        }
        TestManager.addToRewrite(this);
    }

    public Package pack() {
        Package pack = new Package();
        pack.put("title", test.getTitle(), StringTransfer.INSTANCE);
        Array<Package> results = new Array<>(PackageTransfer.INSTANCE);
        for (UserResult ur : this.results) {
            results.add(ur.pack());
        }
        pack.put("results", results, new ArrayTransfer<>());
        return pack;
    }

    public static TestInfo unpack(Package pack) {
        String title = pack.getString("title");
        Test test;
        if (TestManager.hasTest(title)) {
            test = TestManager.getTestByTitle(title);
        } else {
            test = null;
        }
        if (test == null) {
            throw new RuntimeException("Тест с названием \"" + title + "\" был удалён или переименован");
        }
        TestInfo ti = new TestInfo(test);
        Array<Package> results = pack.getArray("results");
        for (Package result : results) {
            ti.results.add(UserResult.unpack(result));
        }
        return ti;
    }
}
