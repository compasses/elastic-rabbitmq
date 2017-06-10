package elasticsearch.searchservice.models.ESResp;

/**
 * Created by i311352 on 5/4/2017.
 */
public class HitResult {
    HitSource _source;

    public HitSource get_source() {
        return _source;
    }

    public void set_source(HitSource _source) {
        this._source = _source;
    }
}