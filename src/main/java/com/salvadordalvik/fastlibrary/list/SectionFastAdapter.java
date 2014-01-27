package com.salvadordalvik.fastlibrary.list;

import android.app.Activity;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * FastLib
 * Created by Matthew Shepard on 11/17/13.
 */
public class SectionFastAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private Activity act;
    private Fragment frag;
    private FastItem[] combinedItemList = new FastItem[0];
    private ArrayList<ArrayList<FastItem>> sectionItemList = new ArrayList<ArrayList<FastItem>>();
    private LayoutInflater inflater;

    private boolean allEnabled = true;
    private int maxTypeCount;
    private int[] typeList;

    public SectionFastAdapter(Activity activity, Fragment fragment, int maxTypeCount) {
        this.act = activity;
        this.frag = fragment;
        this.typeList = null;
        this.maxTypeCount = maxTypeCount;
        inflater = LayoutInflater.from(activity);
    }

    private int generateViewType(int itemLayout){
        if(typeList == null){
            typeList = new int[]{itemLayout};
            return 0;
        }
        for(int ix=0;ix<typeList.length;ix++){
            if(typeList[ix] == itemLayout){
                return ix;
            }
        }
        if(typeList.length >= maxTypeCount){
            throw new RuntimeException("FastAdapter: Number of unique view types exceed maxTypeCount");
        }
        typeList = Arrays.copyOf(typeList, typeList.length+1);
        typeList[typeList.length-1] = itemLayout;
        return typeList.length-1;
    }

    private void regenerateCombinedList(){
        int newLength = 0, ix = 0;
        for(ArrayList<FastItem> list : sectionItemList){
            newLength += list.size();
        }
        combinedItemList = new FastItem[newLength];
        for(ArrayList<FastItem> list : sectionItemList){
            for(FastItem item : list){
                combinedItemList[ix] = item;
                ix++;
            }
        }
        notifyDataSetChanged();
    }

    private ArrayList<FastItem> getSection(int section){
        if(section < 0){
            throw new IllegalArgumentException("ERROR: invalid section id, cannot be < 0");
        }
        while(sectionItemList.size() <= section){
            sectionItemList.add(new ArrayList<FastItem>());
        }
        return sectionItemList.get(section);
    }

    public void addItems(int section, List<? extends FastItem> list){
        ArrayList<FastItem> itemList = getSection(section);
        itemList.addAll(list);
        for(FastItem item : list){
            allEnabled = allEnabled && item.isEnabled();
            item.setType(generateViewType(item.getLayoutId()));
        }
        regenerateCombinedList();
    }

    public void addItems(int section, FastItem... items){
        ArrayList<FastItem> itemList = getSection(section);
        for(FastItem item : items){
            allEnabled = allEnabled && item.isEnabled();
            item.setType(generateViewType(item.getLayoutId()));
            itemList.add(item);
        }
        regenerateCombinedList();
    }

    public void clearAll(){
        sectionItemList.clear();
        combinedItemList = new FastItem[0];
        allEnabled = true;
        notifyDataSetChanged();
    }

    public void clearSection(int section){
        if(section < sectionItemList.size()){
            sectionItemList.get(section).clear();
            regenerateCombinedList();
        }
    }

    public int getSectionOffset(int section){
        if(section < sectionItemList.size()){
            int count = 0;
            for(int ix=0;ix<section;ix++){
                count += sectionItemList.get(ix).size();
            }
            return count;
        }else{
            return combinedItemList.length;
        }
    }

    @Override
    public int getCount() {
        return combinedItemList.length;
    }

    @Override
    public FastItem getItem(int position) {
        return combinedItemList[position];
    }

    @Override
    public long getItemId(int position) {
        return combinedItemList[position].getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FastItem item = combinedItemList[position];
        Object viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(item.getLayoutId(), parent, false);
            viewHolder = item.generateViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = convertView.getTag();
        }
        item.updateView(convertView, viewHolder);
        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        combinedItemList[position].onItemClick(act, frag);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return allEnabled;
    }

    @Override
    public boolean isEnabled(int position) {
        return combinedItemList[position].isEnabled();
    }

    @Override
    public int getItemViewType(int position) {
        return combinedItemList[position].getType();
    }

    @Override
    public int getViewTypeCount() {
        return maxTypeCount;
    }

    @Override
    public boolean isEmpty() {
        return combinedItemList.length == 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}