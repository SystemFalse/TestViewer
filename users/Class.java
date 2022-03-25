package users;

import java.util.ArrayList;
import java.util.Objects;

public class Class {

    private final ArrayList<Teacher> teachers;
    private final ArrayList<Pupil> pupils;

    private byte number;
    private char postfix;

    Class(String name) {
        setName(name);
        teachers = new ArrayList<>();
        pupils = new ArrayList<>();
    }

    public String getName() {
        return number + " \"" + postfix + '"';
    }

    public void setName(String name) {
        Objects.requireNonNull(name);
        if (name.matches("([1-9]|10|11)\\s\"[А-Я]\"")) {
            if (Character.isDigit(name.charAt(1))) {
                number = Byte.parseByte(name.substring(0, 2));
            } else {
                number = Byte.parseByte(name.substring(0, 1));
            }
            postfix = name.charAt(name.length() - 2);
            return;
        }
        throw new IllegalArgumentException("Недопустимое название класса: " + name);
    }

    public byte getNumber() {
        return number;
    }

    public char getPostfix() {
        return postfix;
    }

    private void checkTeacherIndex(int index) {
        if (index >= teachers.size()) {
            throw new IndexOutOfBoundsException("Индекс " + index + " массива длинной " + teachers.size());
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("Негативный индекс: " + index);
        }
    }

    public int getTeacherCount() {
        return teachers.size();
    }

    public void addTeacher(Teacher teacher) {
        if (!teachers.contains(teacher)) {
            teachers.add(teacher);
            teacher.addClassName0(getName());
        }
    }

    public void setTeacher(Teacher teacher, int index) {
        checkTeacherIndex(index);
        Teacher last = teachers.set(index, teacher);
        String name = getName();
        last.removeClassName(name);
        teacher.addClassName0(name);
    }

    public Teacher getTeacher(int index) {
        checkTeacherIndex(index);
        return teachers.get(index);
    }

    public void removeTeacher(int index) {
        checkTeacherIndex(index);
        teachers.get(index).removeClassName(getName());
        teachers.remove(index);
    }

    public String[] getTeacherNames() {
        return teachers.stream().map(User::getName).toArray(String[]::new);
    }

    public void removeTeacher(Teacher teacher) {
        teacher.removeClassName0(getName());
        teachers.remove(teacher);
    }

    private void checkPupilIndex(int index) {
        if (index >= pupils.size()) {
            throw new IndexOutOfBoundsException("Индекс " + index + " массива длинной " + pupils.size());
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("Негативный индекс: " + index);
        }
    }

    public int getPupilCount() {
        return pupils.size();
    }

    public void addPupil(Pupil pupil) {
        pupils.add(pupil);
        pupil.setClassName0(getName());
    }

    public void setPupil(Pupil pupil, int index) {
        checkPupilIndex(index);
        Pupil last = pupils.set(index, pupil);
        last.setClassName(null);
        pupil.setClassName0(getName());
    }

    public Pupil getPupil(int index) {
        checkPupilIndex(index);
        return pupils.get(index);
    }

    public void removePupil(int index) {
        checkPupilIndex(index);
        pupils.get(index).setClassName(null);
        pupils.remove(index);
    }

    public boolean removePupil(Pupil pupil) {
        String name = getName();
        if (name.equals(pupil.getClassName())) {
            pupil.setClassName(null);
        }
        return pupils.remove(pupil);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Class aClass)) return false;
        if (getNumber() != aClass.getNumber()) return false;
        return getPostfix() == aClass.getPostfix();
    }

    @Override
    public int hashCode() {
        int result = getNumber();
        result = 31 * result + (int) getPostfix();
        return result;
    }

    @Override
    public String toString() {
        return getName();
    }
}
