package users.util;

import users.Pupil;
import users.Teacher;

import java.util.Objects;

public class Class {

    private Teacher[] teachers;
    private Pupil[] pupils;

    private byte number;
    private char postfix;

    Class(String name) {
        setName(name);
        teachers = new Teacher[0];
        pupils = new Pupil[0];
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
        if (index >= teachers.length) {
            throw new IndexOutOfBoundsException("Индекс " + index + " массива длинной " + teachers.length);
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("Негативный индекс: " + index);
        }
    }

    public int getTeacherCount() {
        return teachers.length;
    }

    public void addTeacher(Teacher teacher) {
        Teacher[] newLength = new Teacher[teachers.length + 1];
        System.arraycopy(teachers, 0, newLength, 0, teachers.length);
        newLength[newLength.length - 1] = teacher;
        teacher.addClassName(getName());
        teachers = newLength;
    }

    public void setTeacher(Teacher teacher, int index) {
        checkTeacherIndex(index);
        teachers[index] = teacher;
    }

    public Teacher getTeacher(int index) {
        checkTeacherIndex(index);
        return teachers[index];
    }

    public void removeTeacher(int index) {
        checkTeacherIndex(index);
        Teacher[] newLength = new Teacher[teachers.length - 1];
        if (index == newLength.length) {
            System.arraycopy(teachers, 0, newLength, 0, index);
        } else {
            System.arraycopy(teachers, 0, newLength, 0, index);
            System.arraycopy(teachers, index + 1, newLength, index, teachers.length - index - 1);
        }
        teachers[index].removeClassName(getName());
        teachers = newLength;
    }

    public String[] getTeacherNames() {
        String[] names = new String[teachers.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = teachers[i].getName();
        }
        return names;
    }

    public boolean removeTeacher(Teacher teacher) {
        for (int i = 0; i < teachers.length; i++) {
            Teacher exist = teachers[i];
            if (exist.equals(teacher)) {
                removeTeacher(i);
                return true;
            }
        }
        return false;
    }

    private void checkPupilIndex(int index) {
        if (index >= pupils.length) {
            throw new IndexOutOfBoundsException("Индекс " + index + " массива длинной " + pupils.length);
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("Негативный индекс: " + index);
        }
    }

    public int getPupilCount() {
        return pupils.length;
    }

    public void addPupil(Pupil pupil) {
        Pupil[] newLength = new Pupil[teachers.length + 1];
        System.arraycopy(pupils, 0, newLength, 0, teachers.length);
        newLength[newLength.length - 1] = pupil;
        pupil.setClassName(getName());
        pupils = newLength;
    }

    public void setPupil(Pupil pupil, int index) {
        checkPupilIndex(index);
        pupils[index] = pupil;
    }

    public Pupil getPupil(int index) {
        checkPupilIndex(index);
        return pupils[index];
    }

    public void removePupil(int index) {
        checkPupilIndex(index);
        Pupil[] newLength = new Pupil[teachers.length - 1];
        if (index == newLength.length) {
            System.arraycopy(pupils, 0, newLength, 0, index);
        } else {
            System.arraycopy(pupils, 0, newLength, 0, index);
            System.arraycopy(pupils, index + 1, newLength, index, teachers.length - index - 1);
        }
        pupils[index].setClassName(null);
        pupils = newLength;
    }

    public boolean removePupil(Pupil pupil) {
        for (int i = 0; i < pupils.length; i++) {
            Pupil exist = pupils[i];
            if (exist.equals(pupil)) {
                removePupil(i);
                return true;
            }
        }
        return false;
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
}
