package com.vaadin.server.data.provider;

import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.server.data.DataProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.data.ListDataProvider;
import com.vaadin.server.data.Query;

public class ListDataProviderTest {

    private ListDataProvider<StrBean> dataProvider;
    private List<StrBean> data;

    @Before
    public void setUp() {
        data = StrBean.generateRandomBeans(100);
        dataProvider = DataProvider.create(data);
    }

    @Test
    public void testListContainsAllData() {
        List<StrBean> list = new LinkedList<>(data);
        dataProvider.fetch(new Query())
                .forEach(str -> assertTrue(
                        "Data provider contained values not in original data",
                        list.remove(str)));
        assertTrue("Not all values from original data were in data provider",
                list.isEmpty());
    }

    @Test
    public void testSortByComparatorListsDiffer() {
        Comparator<StrBean> comp = Comparator.comparing(StrBean::getValue)
                .thenComparing(StrBean::getRandomNumber)
                .thenComparing(StrBean::getId);
        List<StrBean> list = dataProvider.sortingBy(comp).fetch(new Query())
                .collect(Collectors.toList());

        // First value in data is { Xyz, 10, 100 } which should be last in list
        Assert.assertNotEquals("First value should not match", data.get(0),
                list.get(0));

        Assert.assertEquals("Sorted data and original data sizes don't match",
                data.size(), list.size());

        data.sort(comp);
        for (int i = 0; i < data.size(); ++i) {
            Assert.assertEquals("Sorting result differed", data.get(i),
                    list.get(i));
        }
    }

    @Test
    public void testDefatulSortWithSpecifiedPostSort() {
        Comparator<StrBean> comp = Comparator.comparing(StrBean::getValue)
                .thenComparing(Comparator.comparing(StrBean::getId).reversed());
        List<StrBean> list = dataProvider.sortingBy(comp).fetch(new Query())
                // The sort here should come e.g from a Component
                .sorted(Comparator.comparing(StrBean::getRandomNumber))
                .collect(Collectors.toList());

        Assert.assertEquals("Sorted data and original data sizes don't match",
                data.size(), list.size());

        for (int i = 1; i < list.size(); ++i) {
            StrBean prev = list.get(i - 1);
            StrBean cur = list.get(i);
            // Test specific sort
            Assert.assertTrue(prev.getRandomNumber() <= cur.getRandomNumber());

            if (prev.getRandomNumber() == cur.getRandomNumber()) {
                // Test default sort
                Assert.assertTrue(
                        prev.getValue().compareTo(cur.getValue()) <= 0);
                if (prev.getValue().equals(cur.getValue())) {
                    Assert.assertTrue(prev.getId() > cur.getId());
                }
            }
        }
    }

    @Test
    public void testDefatulSortWithFunction() {
        List<StrBean> list = dataProvider.sortingBy(StrBean::getValue)
                .fetch(new Query()).collect(Collectors.toList());

        Assert.assertEquals("Sorted data and original data sizes don't match",
                data.size(), list.size());

        for (int i = 1; i < list.size(); ++i) {
            StrBean prev = list.get(i - 1);
            StrBean cur = list.get(i);

            // Test default sort
            Assert.assertTrue(prev.getValue().compareTo(cur.getValue()) <= 0);
        }
    }

    @Test
    public void refreshAll_changeBeanInstance() {
        StrBean bean = new StrBean("foo", -1, hashCode());
        Query query = new Query();
        int size = dataProvider.size(query);

        data.set(0, bean);
        dataProvider.refreshAll();

        List<StrBean> list = dataProvider.fetch(query)
                .collect(Collectors.toList());
        StrBean first = list.get(0);
        Assert.assertEquals(bean.getValue(), first.getValue());
        Assert.assertEquals(bean.getRandomNumber(), first.getRandomNumber());
        Assert.assertEquals(bean.getId(), first.getId());

        Assert.assertEquals(size, dataProvider.size(query));
    }

    @Test
    public void refreshAll_updateBean() {
        Query query = new Query();
        int size = dataProvider.size(query);

        StrBean bean = data.get(0);
        bean.setValue("foo");
        dataProvider.refreshAll();

        List<StrBean> list = dataProvider.fetch(query)
                .collect(Collectors.toList());
        StrBean first = list.get(0);
        Assert.assertEquals("foo", first.getValue());

        Assert.assertEquals(size, dataProvider.size(query));
    }

    @Test
    public void refreshAll_sortingBy_changeBeanInstance() {
        StrBean bean = new StrBean("foo", -1, hashCode());
        Query query = new Query();
        int size = dataProvider.size(query);

        data.set(0, bean);

        ListDataProvider<StrBean> dSource = dataProvider
                .sortingBy(Comparator.comparing(StrBean::getId));
        dSource.refreshAll();

        List<StrBean> list = dSource.fetch(query).collect(Collectors.toList());
        StrBean first = list.get(0);
        Assert.assertEquals(bean.getValue(), first.getValue());
        Assert.assertEquals(bean.getRandomNumber(), first.getRandomNumber());
        Assert.assertEquals(bean.getId(), first.getId());

        Assert.assertEquals(size, dataProvider.size(query));
    }

    @Test
    public void refreshAll_addBeanInstance() {
        StrBean bean = new StrBean("foo", -1, hashCode());

        Query query = new Query();
        int size = dataProvider.size(query);

        data.add(0, bean);
        dataProvider.refreshAll();

        List<StrBean> list = dataProvider.fetch(query)
                .collect(Collectors.toList());
        StrBean first = list.get(0);
        Assert.assertEquals(bean.getValue(), first.getValue());
        Assert.assertEquals(bean.getRandomNumber(), first.getRandomNumber());
        Assert.assertEquals(bean.getId(), first.getId());

        Assert.assertEquals(size + 1, dataProvider.size(query));
    }

    @Test
    public void refreshAll_removeBeanInstance() {
        Query query = new Query();
        int size = dataProvider.size(query);

        data.remove(0);
        dataProvider.refreshAll();

        Assert.assertEquals(size - 1, dataProvider.size(query));
    }
}
