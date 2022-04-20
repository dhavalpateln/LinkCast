package com.dhavalpateln.linkcast.ui.mangas;

import androidx.fragment.app.Fragment;

import com.dhavalpateln.linkcast.uihelpers.AbstractCatalogFragment;


public class MangaFragment extends AbstractCatalogFragment {

    public static class Catalog {
        public static final String READING = "Reading";
        public static final String PLANNED = "Planned";
        public static final String COMPLETED = "Completed";
        public static final String ONHOLD = "On Hold";
        public static final String DROPPED = "Dropped";
        public static final String FAVORITE = "Fav";
        public static final String ALL = "All";
        public static final String[] BASIC_TYPES = {READING, PLANNED, COMPLETED, ONHOLD, DROPPED};
        public static final String[] ALL_TYPES = {READING, PLANNED, FAVORITE, COMPLETED, ONHOLD, DROPPED, ALL};
    }

    @Override
    public Fragment createNewFragment(String tabName) {
        return MangaFragmentObject.newInstance(tabName);
    }

    @Override
    public String[] getTabs() {
        return Catalog.ALL_TYPES;
    }
}
/*public class MangaFragment extends Fragment {

    private List<AnimeLinkData> dataList;
    private ListRecyclerAdapter<AnimeLinkData> recyclerAdapter;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_manga, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dataList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.manga_list_recycler_view);

        recyclerAdapter = new ListRecyclerAdapter<>(dataList, getContext(), new String[] {"OPEN", "DELETE"},(ListRecyclerAdapter.RecyclerInterface<AnimeLinkData>) (holder, position, data) -> {
            holder.titleTextView.setText(data.getTitle());
            Glide.with(getContext())
                    .load(data.getAnimeMetaData(AnimeLinkData.DataContract.DATA_IMAGE_URL))
                    .centerCrop()
                    .crossFade()
                    //.bitmapTransform(new CropCircleTransformation(getApplicationContext()))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imageView);
            holder.imageView.setClipToOutline(true);

            holder.getButton("OPEN").setOnClickListener(v -> {
                Intent intent = MangaAdvancedView.prepareIntent(getContext(), data);
                startActivity(intent);
            });

            holder.getButton("DELETE").setOnClickListener(v -> {
                FirebaseDBHelper.removeMangaLink(data.getId());
            });

        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recyclerAdapter);

        MangaDataViewModel viewModel = new ViewModelProvider(getActivity()).get(MangaDataViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), stringAnimeLinkDataMap -> {
            dataList.clear();
            for(Map.Entry<String, AnimeLinkData> entry: stringAnimeLinkDataMap.entrySet()) {
                dataList.add(entry.getValue());
            }
            recyclerAdapter.notifyDataSetChanged();
        });
    }
}*/