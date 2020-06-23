package org.apache.camel.quarkus.component.nitrite.it;

import org.apache.camel.component.nitrite.operation.CollectionOperation;
import org.apache.camel.component.nitrite.operation.RepositoryOperation;
import org.apache.camel.component.nitrite.operation.collection.FindCollectionOperation;
import org.apache.camel.component.nitrite.operation.collection.RemoveCollectionOperation;
import org.apache.camel.component.nitrite.operation.collection.UpdateCollectionOperation;
import org.apache.camel.component.nitrite.operation.common.InsertOperation;
import org.apache.camel.component.nitrite.operation.repository.FindRepositoryOperation;
import org.apache.camel.component.nitrite.operation.repository.RemoveRepositoryOperation;
import org.apache.camel.component.nitrite.operation.repository.UpdateRepositoryOperation;
import org.dizitart.no2.Document;
import org.dizitart.no2.filters.Filters;
import org.dizitart.no2.objects.filters.ObjectFilters;

public class Operation {

    enum Type {
        update, find, delete, findGt, insert
    };

    private Type type;

    private String field;

    private Object value;

    private Employee employee;
    private Document document;

    public Operation() {
    }

    private Operation(Type type, String field, Object value) {
        this.type = type;
        this.field = field;
        this.value = value;
    }

    public Operation(Type type, String field, Object value, Employee employee) {
        this(type, field, value);
        this.employee = employee;
    }

    public Operation(Type type, String field, Object value, Document document) {
        this(type, field, value);
        this.document = document;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public RepositoryOperation toRepositoryOperation() {

        switch (type) {
        case update:
            return new UpdateRepositoryOperation(ObjectFilters.eq(field, value));
        case find:
            return new FindRepositoryOperation(ObjectFilters.eq(field, value));
        case findGt:
            return new FindRepositoryOperation(ObjectFilters.gt(field, value));
        case delete:
            return new RemoveRepositoryOperation(ObjectFilters.eq(field, value));
        default:
            throw new UnsupportedOperationException();
        }
    }

    public CollectionOperation toCollectionOperation() {
        switch (type) {
        case update:
            return new UpdateCollectionOperation(Filters.eq(field, value));
        case find:
            return new FindCollectionOperation(Filters.eq(field, value));
        case delete:
            return new RemoveCollectionOperation(Filters.eq(field, value));
        case insert:
            return new InsertOperation();
        default:
            throw new UnsupportedOperationException();
        }
    }
}
