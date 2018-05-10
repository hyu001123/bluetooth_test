package com.example.administrator.bluetooth_test.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.content.Context;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.bluetooth_test.ConnectThread;
import com.example.administrator.bluetooth_test.MainActivity;
import com.example.administrator.bluetooth_test.R;

import java.util.List;


public class BTAdapter extends BaseAdapter{
    private final Context context;
    private final List<BluetoothDevice> listDevice;
    private ViewHolder holder;
    private OnBluetoothConnectCallback BTConnectcallback;

    public BTAdapter(Context context, List<BluetoothDevice> listDevice){
        this.context=context;
        this.listDevice=listDevice;

    }
    /**
     * How many items are in the data set represented by this Adapter.
     *
     * @return Count of items.
     */
    @Override
    public int getCount() {
        return listDevice.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return listDevice.get(position);
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position The position of the item within the adapter's data set whose row id we want.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param parent      The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        holder=null;
        if(convertView==null){
            holder=new ViewHolder();
            convertView=View.inflate(context, R.layout.item_device,null);
            holder.btName=(TextView)convertView.findViewById(R.id.tv_name);
            holder.btAddress=(TextView)convertView.findViewById(R.id.tv_address);
            holder.btState=(TextView)convertView.findViewById(R.id.tv_connState);
            holder.btButton=(Button)convertView.findViewById(R.id.btn_connect);
            //将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
            holder.btName.setText(listDevice.get(position).getName());
            holder.btAddress.setText(listDevice.get(position).getAddress());
            if(listDevice.get(position).getBondState()==12){
                holder.btState.setText("已配对");
                holder.btButton.setVisibility(View.GONE);
            }else{
                holder.btState.setText("未配对");
                holder.btButton.setVisibility(View.VISIBLE);
            }

            holder.btButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectThread thread = new ConnectThread(context, listDevice.get(position));
                    thread.start();
                       holder.btState.setText("已配对");
                       holder.btButton.setVisibility(View.GONE);
                    Toast.makeText(context,"正在连接中.....",Toast.LENGTH_LONG).show();
                }
            });
        return convertView;
    }

    class ViewHolder{
        private TextView btName;
        private TextView btAddress;
        private TextView btState;
        private Button btButton;
    }

    public void setOnBluetoothConnect(OnBluetoothConnectCallback callback){
        BTConnectcallback=callback;
    }

    public interface OnBluetoothConnectCallback{
        void onBluetoothConnect(ViewHolder holder);
    }


}
