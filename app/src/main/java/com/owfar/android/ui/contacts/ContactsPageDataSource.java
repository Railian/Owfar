package com.owfar.android.ui.contacts;

public enum ContactsPageDataSource {

    //region instances
    OWFAR("OWFAR"),
    ALL("ALL");
    //endregion

    //region fields
    private String title;
    private ContactItemAdapter itemAdapter;
    private static String searchText;
    //endregion

    //region Constructors
    ContactsPageDataSource(String title) {
        this.title = title;
        itemAdapter = new ContactItemAdapter(false, true);
    }
    //endregion

    //region Getters And Setters
    public String getTitle() {
        return title;
    }

    public ContactItemAdapter getItemAdapter() {
        return itemAdapter;
    }

    public static String getSearchText() {
        return searchText;
    }

    public static void setSearchText(String searchText) {
        ContactsPageDataSource.searchText = searchText;
        for (ContactsPageDataSource dataSource : values())
            if (dataSource.itemAdapter != null) dataSource.itemAdapter.setSearchText(searchText);
    }

    public static void setOnContactClickListener(ContactItemHolder.OnContactClickListener listener) {
        for (ContactsPageDataSource dataSource : values())
            if (dataSource.itemAdapter != null)
                dataSource.itemAdapter.setOnContactClickListener(listener);
    }
    //endregion
}