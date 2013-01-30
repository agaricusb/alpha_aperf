package ee.lutsu.alpha.mc.aperf.sys.objects;

public class EntityClassMapEntry<T, K, L>
{
	public T key;
	public K mid;
	public L value;
	
	public EntityClassMapEntry(T pKey, K pMid, L pValue)
	{
		key = pKey;
		mid = pMid;
		value = pValue;
	}
}